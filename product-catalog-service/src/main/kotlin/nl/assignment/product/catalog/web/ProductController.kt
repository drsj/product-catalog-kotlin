package nl.assignment.product.catalog.web

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import nl.assignment.product.catalog.service.ProductService
import nl.assignment.product.catalog.dto.ProductRequest
import nl.assignment.product.catalog.dto.ProductResponse
import nl.assignment.product.catalog.dto.ProductSearchResponse
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.JsonNode

@Validated
@RestController
@RequestMapping("/api/products")
class ProductController(
    private val service: ProductService
) {

    private val logger = LoggerFactory.getLogger(ProductController::class.java)

    /**
     * Create a new product.
     *
     * - Accepts a ProductRequest DTO from the client.
     * - Converts the DTO into a Product entity.
     * - Delegates creation to the ProductService.
     * - Returns HTTP 201 (Created) with the newly created product.
     */
    @PostMapping
    fun create(@RequestBody @Valid request: ProductRequest): ResponseEntity<ProductResponse> {
        val created = service.create(request.toEntity())
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    data class QuantityDeltaRequest(
        val delta: Long
    )

    /**
     * Update only the quantity of an existing product.
     *
     * - Uses PATCH because this is a partial update.
     * - Identifies the product by its SKU.
     * - Accepts the quantity delta either as a query parameter or as JSON body.
     * - Delegates the update logic to the ProductService.
     * - Returns HTTP 200 with a confirmation message.
     */
    @PatchMapping("/{sku}/quantity")
    fun updateQuantity(
        @PathVariable sku: String,
        @RequestBody(required = false) body: QuantityDeltaRequest?,
        @RequestParam(required = false) delta: Long?
    ): ResponseEntity<Map<String, String>> {

        val resolvedDelta = delta ?: body?.delta
        ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "delta is required")

        service.updateQuantity(sku, resolvedDelta)

        val response = mapOf(
            "message" to "Product quantity successfully updated",
            "sku" to sku,
            "delta" to resolvedDelta.toString()
        )

        return ResponseEntity.ok(response)
    }


    /**
     * Retrieve a single product by its SKU.
     *
     * - Returns a ProductResponse DTO.
     */
    @GetMapping("/{sku}")
    fun get(@PathVariable sku: String): ResponseEntity<ProductResponse> =
        ResponseEntity.ok(service.getBySku(sku))

    /**
     * Fully update an existing product.
     *
     */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid request: ProductRequest
    ): ResponseEntity<ProductResponse> =
        ResponseEntity.ok(service.update(id, request.toEntity()))

    /**
     * Retrieve a paginated and optionally sorted list of products.
     *
     * Spring Boot 4.1 no longer auto-binds Pageable parameters,
     * so pagination and sorting are manually constructed.
     */
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) size: Int,
        @RequestParam(required = false) sort: String?
    ): ResponseEntity<Page<ProductResponse>> {

        logger.info("Getting all products: page={}, size={}, sort={}", page, size, sort)

        val pageable: Pageable =
            if (sort != null) {
                val parts = sort.split(",")
                val property = parts[0].trim()
                val direction = if (parts.size > 1) {
                    Sort.Direction.fromString(parts[1].trim())
                } else {
                    Sort.Direction.ASC
                }
                PageRequest.of(page, size, Sort.by(direction, property))
            } else {
                PageRequest.of(page, size)
            }

        return ResponseEntity.ok(service.list(pageable))
    }

    /**
     * Deletes a product identified by its SKU.
     *
     * - Returns HTTP 204 when deletion succeeds.
     */
    @DeleteMapping("/{sku}")
    fun deleteProduct(@PathVariable sku: String): ResponseEntity<Void> {
        service.deleteProduct(sku)
        return ResponseEntity.noContent().build()
    }

    /**
     * Full-text search endpoint backed by Elasticsearch.
     *
     * - Accepts a free-text query parameter "q".
     * - Supports pagination via manually constructed Pageable.
     */
    @GetMapping("/search")
    fun search(
        @RequestParam("q") query: String,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) size: Int
    ): ResponseEntity<Page<ProductSearchResponse>> {

        val pageable = PageRequest.of(page, size)
        return ResponseEntity.ok(service.search(query, pageable))
    }
}