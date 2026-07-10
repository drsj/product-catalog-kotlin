package nl.assignment.pricingmock

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.RepeatedTest
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PricingMockControllerTest {

    private val controller = PricingMockController()

    @Test
    fun `getPrice should return response with correct SKU`() {
        val sku = "TEST-SKU-123"
        val basePrice = BigDecimal("100.00")

        val response = controller.getPrice(sku, basePrice)

        assertEquals(sku, response.sku)
    }

    @Test
    fun `getPrice should return price within expected range`() {
        val basePrice = BigDecimal("100.00")
        val minExpected = basePrice + BigDecimal("-5.0")
        val maxExpected = basePrice + BigDecimal("5.0")

        val response = controller.getPrice("SKU1", basePrice)

        assertTrue(response.price >= minExpected)
        assertTrue(response.price <= maxExpected)
    }

    @RepeatedTest(10)
    fun `getPrice should vary prices for multiple calls`() {
        val basePrice = BigDecimal("50.00")
        val prices = mutableSetOf<BigDecimal>()

        repeat(10) {
            val response = controller.getPrice("SKU-VAR", basePrice)
            prices.add(response.price)
        }

        // With 10 calls, should have some variation (unlikely to be all identical)
        assertTrue(prices.size > 1, "Price should vary across multiple calls")
    }

    @Test
    fun `getPrice should handle high base prices`() {
        val basePrice = BigDecimal("5000.00")
        val response = controller.getPrice("EXPENSIVE", basePrice)

        val minExpected = basePrice + BigDecimal("-5.0")
        val maxExpected = basePrice + BigDecimal("5.0")

        assertTrue(response.price >= minExpected)
        assertTrue(response.price <= maxExpected)
    }

    @Test
    fun `getPrice should handle low base prices`() {
        val basePrice = BigDecimal("0.50")
        val response = controller.getPrice("CHEAP", basePrice)

        val minExpected = basePrice + BigDecimal("-5.0")
        val maxExpected = basePrice + BigDecimal("5.0")

        assertTrue(response.price >= minExpected)
        assertTrue(response.price <= maxExpected)
    }

    @Test
    fun `getPrice should handle SKU with special characters`() {
        val sku = "SKU-2024/SPECIAL_v1.0"
        val basePrice = BigDecimal("99.99")

        val response = controller.getPrice(sku, basePrice)

        assertEquals(sku, response.sku)
    }

    @Test
    fun `getPrice response price should never be negative for reasonable base price`() {
        val basePrice = BigDecimal("10.00")

        repeat(20) {
            val response = controller.getPrice("POSITIVE", basePrice)
            // With base price of 10 and max variance of -5, price could theoretically be 5+
            assertTrue(response.price >= BigDecimal("5.00"))
        }
    }
}
