package nl.assignment.product.catalog.dto

import java.math.BigDecimal

data class ProductSearchResponse(
    val sku: String,
    val name: String,
    val description: String?,
    val brand: String?,
    val category: String?,
    val price: BigDecimal,
    val quantity: Long,
    val currency: String?,
    val score: Float? = null
)
