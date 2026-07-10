package nl.assignment.product.catalog.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ProductMapperTest {

    @Test
    fun `toResponse should convert Product to ProductResponse correctly`() {
        val instant = Instant.now()
        val product = Product(
            id = 1L,
            sku = "SKU123",
            name = "Test Product",
            description = "A test product",
            brand = "TestBrand",
            category = "Electronics",
            price = BigDecimal("199.99"),
            quantity = 25,
            currency = "EUR",
            lastSyncedAt = instant
        )

        val response = product.toResponse()

        assertEquals(1L, response.id)
        assertEquals("SKU123", response.sku)
        assertEquals("Test Product", response.name)
        assertEquals("A test product", response.description)
        assertEquals("TestBrand", response.brand)
        assertEquals("Electronics", response.category)
        assertEquals(BigDecimal("199.99"), response.price)
        assertEquals(25L, response.quantity)
        assertEquals("EUR", response.currency)
        assertEquals(instant, response.lastSyncedAt)
    }

    @Test
    fun `toResponse should handle null optional fields`() {
        val product = Product(
            id = 2L,
            sku = "SKU456",
            name = "Minimal Product",
            description = null,
            brand = null,
            category = null,
            price = BigDecimal("49.99"),
            quantity = 5,
            currency = null,
            lastSyncedAt = null
        )

        val response = product.toResponse()

        assertEquals(2L, response.id)
        assertEquals("SKU456", response.sku)
        assertEquals("Minimal Product", response.name)
        assertEquals(null, response.description)
        assertEquals(null, response.brand)
        assertEquals(null, response.category)
        assertEquals(BigDecimal("49.99"), response.price)
        assertEquals(5L, response.quantity)
        assertEquals(null, response.currency)
        assertEquals(null, response.lastSyncedAt)
    }

    @Test
    fun `toResponse should throw error when product has no id`() {
        val product = Product(
            id = null,
            sku = "SKU789",
            name = "Unsaved Product",
            price = BigDecimal("99.99"),
            quantity = 10
        )

        val exception = assertThrows<IllegalStateException> {
            product.toResponse()
        }

        assertNotNull(exception.message)
    }

    @Test
    fun `toResponse should preserve all BigDecimal precision`() {
        val product = Product(
            id = 3L,
            sku = "PRECISION",
            name = "High Precision",
            price = BigDecimal("123.45"),
            quantity = 1
        )

        val response = product.toResponse()

        assertEquals(BigDecimal("123.45"), response.price)
    }
}
