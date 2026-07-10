package nl.assignment.product.catalog.domain

import nl.assignment.product.catalog.dto.ProductResponse

fun Product.toResponse(): ProductResponse =
    ProductResponse(
        id = id ?: error("Product has no id — was it saved?"),
        sku = sku,
        name = name,
        description = description,
        brand = brand,
        category = category,
        price = price,
        quantity = quantity,
        currency = currency,
        lastSyncedAt = lastSyncedAt
    )