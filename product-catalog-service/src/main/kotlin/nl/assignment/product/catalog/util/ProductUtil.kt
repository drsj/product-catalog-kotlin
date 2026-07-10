package nl.assignment.product.catalog.util

import nl.assignment.product.catalog.domain.Product
import java.math.BigDecimal

object ProductUtil {

    fun getProduct(
        sku: String,
        name: String,
        description: String,
        brand: String,
        category: String,
        price: BigDecimal,
        quantity: Long
    ): Product {
        return Product(
            sku = sku,
            name = name,
            description = description,
            brand = brand,
            category = category,
            price = price,
            quantity = quantity,
            currency = "EUR"
        )
    }
}