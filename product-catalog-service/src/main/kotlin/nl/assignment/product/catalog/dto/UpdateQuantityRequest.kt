package nl.assignment.product.catalog.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull

data class UpdateQuantityRequest(
    @field:NotNull(message = "Delta is required")
    @JsonProperty("delta")
    @JsonAlias("quantity")
    val delta: Long
)