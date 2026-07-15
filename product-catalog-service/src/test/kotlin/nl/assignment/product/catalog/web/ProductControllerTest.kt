package nl.assignment.product.catalog.web

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.assignment.product.catalog.dto.ProductResponse
import nl.assignment.product.catalog.service.ProductService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import kotlin.test.assertEquals
import tools.jackson.databind.ObjectMapper

class ProductControllerTest {

    private lateinit var productService: ProductService
    private lateinit var controller: ProductController

    @BeforeEach
    fun setup() {
        productService = mockk()
        controller = ProductController(productService)
    }

    @Test
    fun `list should use default page 0 and size 20`() {
        val product = ProductResponse(
            id = 1L,
            sku = "SKU123",
            name = "Product",
            description = null,
            brand = null,
            category = null,
            price = BigDecimal("99.99"),
            quantity = 10,
            currency = null,
            lastSyncedAt = null
        )
        val pageable = PageRequest.of(0, 20)
        val page = PageImpl(listOf(product), pageable, 1)

        every { productService.list(pageable) } returns page

        val result = controller.list(page = 0, size = 20, sort = null)

        assertEquals(200, result.statusCode.value())
        assertEquals(1, result.body!!.totalElements)
        verify { productService.list(any()) }
    }

    @Test
    fun `list should parse sort parameter correctly with direction`() {
        val product = ProductResponse(
            id = 1L,
            sku = "SKU123",
            name = "Product",
            description = null,
            brand = null,
            category = null,
            price = BigDecimal("99.99"),
            quantity = 10,
            currency = null,
            lastSyncedAt = null
        )
        val sortOrder = Sort.by(Sort.Direction.DESC, "name")
        val pageable = PageRequest.of(0, 20, sortOrder)
        val page = PageImpl(listOf(product), pageable, 1)

        every { productService.list(pageable) } returns page

        val result = controller.list(page = 0, size = 20, sort = "name,DESC")

        assertEquals(200, result.statusCode.value())
        verify { productService.list(match { it.sort.getOrderFor("name")?.direction == Sort.Direction.DESC }) }
    }

    @Test
    fun `list should default sort direction to ASC when not specified`() {
        val product = ProductResponse(
            id = 1L,
            sku = "SKU123",
            name = "Product",
            description = null,
            brand = null,
            category = null,
            price = BigDecimal("99.99"),
            quantity = 10,
            currency = null,
            lastSyncedAt = null
        )
        val sortOrder = Sort.by(Sort.Direction.ASC, "sku")
        val pageable = PageRequest.of(0, 20, sortOrder)
        val page = PageImpl(listOf(product), pageable, 1)

        every { productService.list(pageable) } returns page

        val result = controller.list(page = 0, size = 20, sort = "sku")

        assertEquals(200, result.statusCode.value())
        verify { productService.list(match { it.sort.getOrderFor("sku")?.direction == Sort.Direction.ASC }) }
    }

    @Test
    fun `list should handle multiple sort fields with proper trimming`() {
        val product = ProductResponse(
            id = 1L,
            sku = "SKU123",
            name = "Product",
            description = null,
            brand = null,
            category = null,
            price = BigDecimal("99.99"),
            quantity = 10,
            currency = null,
            lastSyncedAt = null
        )
        val sortOrder = Sort.by(Sort.Direction.ASC, "price")
        val pageable = PageRequest.of(0, 20, sortOrder)
        val page = PageImpl(listOf(product), pageable, 1)

        every { productService.list(pageable) } returns page

        val result = controller.list(page = 0, size = 20, sort = " price , ASC ")

        assertEquals(200, result.statusCode.value())
        verify { productService.list(match { it.sort.getOrderFor("price") != null }) }
    }

    @Test
    fun `list should respect custom page and size parameters`() {
        val product = ProductResponse(
            id = 1L,
            sku = "SKU123",
            name = "Product",
            description = null,
            brand = null,
            category = null,
            price = BigDecimal("99.99"),
            quantity = 10,
            currency = null,
            lastSyncedAt = null
        )
        val pageable = PageRequest.of(2, 50)
        val page = PageImpl(listOf(product), pageable, 100)

        every { productService.list(pageable) } returns page

        val result = controller.list(page = 2, size = 50, sort = null)

        assertEquals(200, result.statusCode.value())
        verify { productService.list(match { it.pageNumber == 2 && it.pageSize == 50 }) }
    }

    @Test
    fun `update quantity should return bad request when delta is missing`() {
        val ex = assertThrows<ResponseStatusException> {
            controller.updateQuantity("APL-IPH-17", null, null)
        }

        assertEquals(400, ex.statusCode.value())
        verify(exactly = 0) { productService.updateQuantity(any(), any()) }
    }

    @Test
    fun `update quantity should use request body delta when provided`() {
        every { productService.updateQuantity("APL-IPH-17", 5L) } returns Unit

        val body = ProductController.QuantityDeltaRequest(delta = 5)
        val result = controller.updateQuantity("APL-IPH-17", body, null)

        assertEquals(204, result.statusCode.value())
        verify { productService.updateQuantity("APL-IPH-17", 5L) }
    }
}
