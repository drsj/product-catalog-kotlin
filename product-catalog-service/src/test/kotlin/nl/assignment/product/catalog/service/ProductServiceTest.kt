package nl.assignment.product.catalog.service

import io.mockk.*
import jakarta.persistence.EntityNotFoundException
import nl.assignment.product.catalog.domain.Product
import nl.assignment.product.catalog.exception.QuantityUpdateException
import nl.assignment.product.catalog.repository.ProductRepository
import nl.assignment.product.catalog.search.ProductDeleteEvent
import nl.assignment.product.catalog.search.ProductIndexEvent
import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductServiceTest {

    private lateinit var productRepository: ProductRepository
    private lateinit var eventPublisher: ApplicationEventPublisher
    private lateinit var es: ElasticsearchClient
    private lateinit var service: ProductService

    @BeforeEach
    fun setup() {
        productRepository = mockk()
        eventPublisher = mockk(relaxed = true)
        es = mockk(relaxed = true)   // DSL-client mock
        service = ProductService(productRepository, eventPublisher, es)
    }

    // ============== CREATE TESTS ==============

    @Test
    fun `create should save product and publish indexing event`() {
        val product = Product(
            sku = "SKU123",
            name = "Test Product",
            price = BigDecimal("99.99"),
            quantity = 10
        )
        val savedProduct = product.copy(id = 1L)

        every { productRepository.save(product) } returns savedProduct
        every { eventPublisher.publishEvent(any<ProductIndexEvent>()) } just Runs

        val result = service.create(product)

        assertEquals(1L, result.id)
        assertEquals("SKU123", result.sku)
        verify { productRepository.save(product) }
        verify { eventPublisher.publishEvent(match<ProductIndexEvent> { it.product.id == 1L }) }
    }

    // ============== UPDATE TESTS ==============

    @Test
    fun `update should modify existing product and publish event`() {
        val existing = Product(
            id = 1L,
            sku = "SKU123",
            name = "Old Name",
            price = BigDecimal("50.00"),
            quantity = 5
        )
        val updateRequest = Product(
            sku = "SKU123",
            name = "New Name",
            price = BigDecimal("99.99"),
            quantity = 20
        )
        val updated = existing.copy(
            name = "New Name",
            price = BigDecimal("99.99"),
            quantity = 20
        )

        every { productRepository.findById(1L) } returns Optional.of(existing)
        every { productRepository.save(any()) } returns updated
        every { eventPublisher.publishEvent(any<ProductIndexEvent>()) } just Runs

        val result = service.update(1L, updateRequest)

        assertEquals("New Name", result.name)
        assertEquals(BigDecimal("99.99"), result.price)
        verify { productRepository.save(any()) }
        verify { eventPublisher.publishEvent(any<ProductIndexEvent>()) }
    }

    @Test
    fun `update should throw EntityNotFoundException for non-existent product`() {
        every { productRepository.findById(999L) } returns Optional.empty()

        assertThrows<EntityNotFoundException> {
            service.update(999L, Product(sku = "TEST", name = "Test", price = BigDecimal.ONE, quantity = 0))
        }
    }

    // ============== UPDATE QUANTITY TESTS ==============

    @Test
    fun `updateQuantity should increase quantity successfully`() {
        val product = Product(
            sku = "SKU123",
            name = "Product",
            quantity = 10,
            price = BigDecimal.TEN
        )
        val updated = product.copy(quantity = 15)

        every { productRepository.findBySku("SKU123") } returns product
        every { productRepository.save(any()) } returns updated
        every { eventPublisher.publishEvent(any<ProductIndexEvent>()) } just Runs

        service.updateQuantity("SKU123", 5)

        verify { productRepository.save(match { it.quantity == 15.toLong() }) }
        verify { eventPublisher.publishEvent(any<ProductIndexEvent>()) }
    }

    @Test
    fun `updateQuantity should decrease quantity successfully`() {
        val product = Product(
            sku = "SKU123",
            name = "Product",
            quantity = 10,
            price = BigDecimal.TEN
        )
        val updated = product.copy(quantity = 7)

        every { productRepository.findBySku("SKU123") } returns product
        every { productRepository.save(any()) } returns updated
        every { eventPublisher.publishEvent(any<ProductIndexEvent>()) } just Runs

        service.updateQuantity("SKU123", -3)

        verify { productRepository.save(match { it.quantity == 7.toLong() }) }
    }

    @Test
    fun `updateQuantity should throw QuantityUpdateException when result would be negative`() {
        val product = Product(
            sku = "SKU123",
            name = "Product",
            quantity = 10,
            price = BigDecimal.TEN
        )

        every { productRepository.findBySku("SKU123") } returns product

        val exception = assertThrows<QuantityUpdateException> {
            service.updateQuantity("SKU123", -15)
        }

        assertTrue(exception.message!!.contains("Quantity cannot become negative"))
        verify(inverse = true) { productRepository.save(any()) }
    }

    @Test
    fun `updateQuantity should allow decreasing to exactly zero`() {
        val product = Product(
            sku = "SKU123",
            name = "Product",
            quantity = 10,
            price = BigDecimal.TEN
        )
        val updated = product.copy(quantity = 0)

        every { productRepository.findBySku("SKU123") } returns product
        every { productRepository.save(any()) } returns updated
        every { eventPublisher.publishEvent(any<ProductIndexEvent>()) } just Runs

        service.updateQuantity("SKU123", -10)

        verify { productRepository.save(match { it.quantity == 0.toLong() }) }
    }

    @Test
    fun `updateQuantity should throw EntityNotFoundException for non-existent product`() {
        every { productRepository.findBySku("NONEXISTENT") } returns null

        assertThrows<EntityNotFoundException> {
            service.updateQuantity("NONEXISTENT", 5)
        }
    }

    // ============== UPDATE PRICE TESTS ==============

    @Test
    fun `updatePrice should update price and set lastSyncedAt timestamp`() {
        val product = Product(
            sku = "SKU123",
            name = "Product",
            price = BigDecimal("50.00"),
            quantity = 10,
            lastSyncedAt = null
        )
        val updated = product.copy(
            price = BigDecimal("75.00"),
            lastSyncedAt = Instant.now()
        )

        every { productRepository.findBySku("SKU123") } returns product
        every { productRepository.save(any()) } returns updated
        every { eventPublisher.publishEvent(any<ProductIndexEvent>()) } just Runs

        service.updatePrice("SKU123", BigDecimal("75.00"))

        verify {
            productRepository.save(
                match {
                    it.price == BigDecimal("75.00") &&
                            it.lastSyncedAt != null
                }
            )
        }
        verify { eventPublisher.publishEvent(any<ProductIndexEvent>()) }
    }

    @Test
    fun `updatePrice should throw EntityNotFoundException for non-existent product`() {
        every { productRepository.findBySku("NONEXISTENT") } returns null

        assertThrows<EntityNotFoundException> {
            service.updatePrice("NONEXISTENT", BigDecimal("99.99"))
        }
    }

    // ============== GET TESTS ==============

    @Test
    fun `getBySku should return product response`() {
        val product = Product(
            id = 1L,
            sku = "SKU123",
            name = "Product",
            price = BigDecimal("99.99"),
            quantity = 10
        )

        every { productRepository.findBySku("SKU123") } returns product

        val result = service.getBySku("SKU123")

        assertEquals("SKU123", result.sku)
        assertEquals("Product", result.name)
    }

    @Test
    fun `getBySku should throw EntityNotFoundException for non-existent product`() {
        every { productRepository.findBySku("NONEXISTENT") } returns null

        assertThrows<EntityNotFoundException> {
            service.getBySku("NONEXISTENT")
        }
    }

    // ============== DELETE TESTS ==============

    @Test
    fun `deleteProduct should delete product and publish delete event`() {
        val product = Product(
            sku = "SKU123",
            name = "Product",
            price = BigDecimal.TEN,
            quantity = 10
        )

        every { productRepository.findBySku("SKU123") } returns product
        every { productRepository.delete(product) } just Runs
        every { eventPublisher.publishEvent(any<ProductDeleteEvent>()) } just Runs

        service.deleteProduct("SKU123")

        verify { productRepository.delete(product) }
        verify { eventPublisher.publishEvent(match<ProductDeleteEvent> { it.sku == "SKU123" }) }
    }

    @Test
    fun `deleteProduct should throw EntityNotFoundException for non-existent product`() {
        every { productRepository.findBySku("NONEXISTENT") } returns null

        assertThrows<EntityNotFoundException> {
            service.deleteProduct("NONEXISTENT")
        }
    }

    // ============== LIST TESTS ==============

    @Test
    fun `list should return paginated products`() {
        val product = Product(
            id = 1L,
            sku = "SKU123",
            name = "Product",
            price = BigDecimal("99.99"),
            quantity = 10
        )
        val pageable = PageRequest.of(0, 20)
        val page = PageImpl(listOf(product), pageable, 1)

        every { productRepository.findAll(pageable) } returns page

        val result = service.list(pageable)

        assertEquals(1, result.totalElements)
        assertEquals("SKU123", result.content[0].sku)
    }
}