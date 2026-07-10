package nl.assignment.product.catalog.dto

import jakarta.validation.constraints.NotNull

data class UpdateQuantityRequest(
    @field:NotNull(message = "Delta is required")
    val delta: Long
)