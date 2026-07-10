package nl.assignment.product.catalog.integration

import java.math.BigDecimal

data class ExternalPriceDto(
    val sku: String,
    val price: BigDecimal
)