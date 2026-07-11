package nl.assignment.product.catalog.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import nl.assignment.product.catalog.domain.Product
import java.math.BigDecimal

data class ProductRequest(
    @field:NotBlank(message = "SKU is required")
    val sku: String,

    @field:NotBlank(message = "Name is required")
    val name: String,

    val description: String? = null,
    val brand: String? = null,
    val category: String? = null,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @field:Digits(integer = 12, fraction = 2, message = "Price must have at most 2 decimal places")
    val price: BigDecimal,

    @field:Min(value = 0, message = "Quantity cannot be negative")
    val quantity: Long,

    val currency: String? = null
) {
    fun toEntity(): Product =
        Product(
            sku = sku,
            name = name,
            description = description,
            brand = brand,
            category = category,
            price = price,
            quantity = quantity,
            currency = currency
        )
}
