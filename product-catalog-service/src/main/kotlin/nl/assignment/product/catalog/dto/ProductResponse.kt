package nl.assignment.product.catalog.dto

import java.math.BigDecimal
import java.time.Instant

data class ProductResponse(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String?,
    val brand: String?,
    val category: String?,
    val price: BigDecimal,
    val quantity: Long,
    val currency: String?,
    val lastSyncedAt: Instant?
)
