package nl.assignment.product.catalog.search

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import java.math.BigDecimal

@Document(indexName = "products", createIndex = false)
data class ProductDocument(
    @Id
    val sku: String,
    val name: String,
    val description: String?,
    val brand: String?,
    val category: String?,
    val price: BigDecimal,
    val quantity: Long,
    val currency: String?
)
