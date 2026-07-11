package nl.assignment.product.catalog.service

import io.mockk.*
import nl.assignment.product.catalog.domain.Product
import nl.assignment.product.catalog.integration.ExternalPriceClient
import nl.assignment.product.catalog.integration.ExternalPriceDto
import nl.assignment.product.catalog.repository.ProductRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PriceSyncServiceTest {

    private lateinit var productRepository: ProductRepository
    private lateinit var externalClient: ExternalPriceClient
    private lateinit var productService: ProductService
    private lateinit var service: PriceSyncService

    @BeforeEach
    fun setup() {
        productRepository = mockk()
        externalClient = mockk()
        productService = mockk(relaxed = true)
        service = PriceSyncService(productRepository, externalClient, productService)
    }

    @Test
    fun `syncPrices should fetch all products and sync prices concurrently`() {
        val products = listOf(
            Product(sku = "SKU1", name = "Product1", price = BigDecimal("10.00"), quantity = 5),
            Product(sku = "SKU2", name = "Product2", price = BigDecimal("20.00"), quantity = 10),
            Product(sku = "SKU3", name = "Product3", price = BigDecimal("30.00"), quantity = 15)
        )

        every { productRepository.findAll() } returns products
        every { externalClient.getPrice(any(), any()) } returns ExternalPriceDto("SKU", BigDecimal("25.00"))
        every { productService.updatePrice(any(), any()) } just Runs

        service.syncPrices()

        verify { productRepository.findAll() }
        verify(exactly = 3) { externalClient.getPrice(any(), any()) }
    }

    @Test
    fun `syncPrices should handle empty product list`() {
        every { productRepository.findAll() } returns emptyList()

        service.syncPrices()

        verify { productRepository.findAll() }
        verify(inverse = true) { externalClient.getPrice(any(), any()) }
    }

    @Test
    fun `syncPrices should skip update when external client returns null response`() {
        val products = listOf(
            Product(sku = "SKU1", name = "Product1", price = BigDecimal("10.00"), quantity = 5)
        )

        every { productRepository.findAll() } returns products
        every { externalClient.getPrice(any(), any()) } returns null

        service.syncPrices()

        verify(inverse = true) { productService.updatePrice(any(), any()) }
    }

    @Test
    fun `syncPrices should skip update when external client returns non-positive price`() {
        val products = listOf(
            Product(sku = "SKU1", name = "Product1", price = BigDecimal("10.00"), quantity = 5)
        )

        every { productRepository.findAll() } returns products
        every { externalClient.getPrice(any(), any()) } returns ExternalPriceDto("SKU1", BigDecimal("-1.00"))

        service.syncPrices()

        verify(inverse = true) { productService.updatePrice(any(), any()) }
    }

    @Test
    fun `syncPrices should continue after external client exception`() {
        val products = listOf(
            Product(sku = "SKU1", name = "Product1", price = BigDecimal("10.00"), quantity = 5),
            Product(sku = "SKU2", name = "Product2", price = BigDecimal("20.00"), quantity = 10)
        )

        every { productRepository.findAll() } returns products
        every { externalClient.getPrice("SKU1", any()) } throws Exception("External service error")
        every { externalClient.getPrice("SKU2", any()) } returns ExternalPriceDto("SKU2", BigDecimal("21.00"))
        every { productService.updatePrice("SKU2", BigDecimal("21.00")) } just Runs

        service.syncPrices()

        verify { externalClient.getPrice("SKU1", BigDecimal("10.00")) }
        verify { externalClient.getPrice("SKU2", BigDecimal("20.00")) }
        verify { productService.updatePrice("SKU2", BigDecimal("21.00")) }
    }

    @Test
    fun `syncPrices should process products with valid external prices`() {
        val products = listOf(
            Product(sku = "SKU1", name = "Product1", price = BigDecimal("10.00"), quantity = 5)
        )
        val externalPrice = ExternalPriceDto("SKU1", BigDecimal("15.99"))

        every { productRepository.findAll() } returns products
        every { externalClient.getPrice("SKU1", BigDecimal("10.00")) } returns externalPrice
        every { productService.updatePrice("SKU1", BigDecimal("15.99")) } just Runs

        service.syncPrices()

        verify { productService.updatePrice("SKU1", BigDecimal("15.99")) }
    }

    @Test
    fun `syncPrices should pass correct SKU and current price to external client`() {
        val product = Product(sku = "TEST-SKU", name = "Test", price = BigDecimal("99.99"), quantity = 1)
        every { productRepository.findAll() } returns listOf(product)
        every { externalClient.getPrice(any(), any()) } returns ExternalPriceDto("TEST-SKU", BigDecimal("100.00"))
        every { productService.updatePrice(any(), any()) } just Runs

        service.syncPrices()

        verify { externalClient.getPrice("TEST-SKU", BigDecimal("99.99")) }
    }
}
