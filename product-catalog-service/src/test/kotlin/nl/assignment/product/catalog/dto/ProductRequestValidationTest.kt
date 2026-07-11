package nl.assignment.product.catalog.dto

import jakarta.validation.Validator
import jakarta.validation.Validation
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductRequestValidationTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `valid ProductRequest should pass validation`() {
        val request = ProductRequest(
            sku = "SKU123",
            name = "Valid Product",
            description = "A valid product",
            brand = "BrandName",
            category = "Category",
            price = BigDecimal("99.99"),
            quantity = 10,
            currency = "EUR"
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `ProductRequest with empty SKU should fail validation`() {
        val request = ProductRequest(
            sku = "",
            name = "Product",
            price = BigDecimal("99.99"),
            quantity = 10
        )

        val violations = validator.validate(request)

        assertEquals(1, violations.size)
        assertTrue(violations.any { it.propertyPath.toString() == "sku" })
    }

    @Test
    fun `ProductRequest with blank SKU should fail validation`() {
        val request = ProductRequest(
            sku = "   ",
            name = "Product",
            price = BigDecimal("99.99"),
            quantity = 10
        )

        val violations = validator.validate(request)

        assertEquals(1, violations.size)
        assertTrue(violations.any { it.propertyPath.toString() == "sku" })
    }

    @Test
    fun `ProductRequest with empty name should fail validation`() {
        val request = ProductRequest(
            sku = "SKU123",
            name = "",
            price = BigDecimal("99.99"),
            quantity = 10
        )

        val violations = validator.validate(request)

        assertEquals(1, violations.size)
        assertTrue(violations.any { it.propertyPath.toString() == "name" })
    }

    @Test
    fun `ProductRequest with price zero should fail validation`() {
        val request = ProductRequest(
            sku = "SKU123",
            name = "Product",
            price = BigDecimal.ZERO,
            quantity = 10
        )

        val violations = validator.validate(request)

        assertEquals(1, violations.size)
        assertTrue(violations.any { it.propertyPath.toString() == "price" })
    }

    @Test
    fun `ProductRequest with negative price should fail validation`() {
        val request = ProductRequest(
            sku = "SKU123",
            name = "Product",
            price = BigDecimal("-10.00"),
            quantity = 10
        )

        val violations = validator.validate(request)

        assertEquals(1, violations.size)
        assertTrue(violations.any { it.propertyPath.toString() == "price" })
    }

    @Test
    fun `ProductRequest with more than 2 decimal places should fail validation`() {
        val request = ProductRequest(
            sku = "SKU123",
            name = "Product",
            price = BigDecimal("10.123"),
            quantity = 10
        )

        val violations = validator.validate(request)

        assertEquals(1, violations.size)
        assertTrue(violations.any { it.propertyPath.toString() == "price" })
    }

    @Test
    fun `ProductRequest with negative quantity should fail validation`() {
        val request = ProductRequest(
            sku = "SKU123",
            name = "Product",
            price = BigDecimal("99.99"),
            quantity = -5
        )

        val violations = validator.validate(request)

        assertEquals(1, violations.size)
        assertTrue(violations.any { it.propertyPath.toString() == "quantity" })
    }

    @Test
    fun `ProductRequest with zero quantity should pass validation`() {
        val request = ProductRequest(
            sku = "SKU123",
            name = "Product",
            price = BigDecimal("99.99"),
            quantity = 0
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `toEntity should convert ProductRequest to Product correctly`() {
        val request = ProductRequest(
            sku = "SKU123",
            name = "Test Product",
            description = "A test",
            brand = "TestBrand",
            category = "Electronics",
            price = BigDecimal("199.99"),
            quantity = 25,
            currency = "EUR"
        )

        val product = request.toEntity()

        assertEquals("SKU123", product.sku)
        assertEquals("Test Product", product.name)
        assertEquals("A test", product.description)
        assertEquals("TestBrand", product.brand)
        assertEquals("Electronics", product.category)
        assertEquals(BigDecimal("199.99"), product.price)
        assertEquals(25L, product.quantity)
        assertEquals("EUR", product.currency)
    }

    @Test
    fun `toEntity should handle null optional fields`() {
        val request = ProductRequest(
            sku = "SKU123",
            name = "Test Product",
            description = null,
            brand = null,
            category = null,
            price = BigDecimal("99.99"),
            quantity = 10,
            currency = null
        )

        val product = request.toEntity()

        assertEquals("SKU123", product.sku)
        assertEquals("Test Product", product.name)
        assertEquals(null, product.description)
        assertEquals(null, product.brand)
        assertEquals(null, product.category)
        assertEquals(null, product.currency)
    }
}
