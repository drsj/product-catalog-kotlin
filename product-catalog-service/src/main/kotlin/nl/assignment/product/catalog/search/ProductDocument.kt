package nl.assignment.product.catalog.search

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import java.math.BigDecimal

data class ProductDocument @JsonCreator constructor(
    @JsonProperty("sku")
    @Id
    val sku: String,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("description")
    val description: String?,
    @JsonProperty("brand")
    val brand: String?,
    @JsonProperty("category")
    val category: String?,
    @JsonProperty("price")
    val price: BigDecimal,
    @JsonProperty("quantity")
    val quantity: Long,
    @JsonProperty("currency")
    val currency: String?
)