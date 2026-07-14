package nl.assignment.product.catalog.service


import co.elastic.clients.elasticsearch.ElasticsearchClient
import nl.assignment.product.catalog.search.ProductDocument
import jakarta.persistence.EntityNotFoundException
import nl.assignment.product.catalog.domain.toResponse
import nl.assignment.product.catalog.dto.ProductResponse
import nl.assignment.product.catalog.dto.ProductSearchResponse
import nl.assignment.product.catalog.repository.ProductRepository
import nl.assignment.product.catalog.domain.Product
import nl.assignment.product.catalog.exception.QuantityUpdateException
import nl.assignment.product.catalog.search.ProductDeleteEvent
import nl.assignment.product.catalog.search.ProductIndexEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val es: ElasticsearchClient
) {

    @Transactional
    fun create(entity: Product): ProductResponse {
        val saved = productRepository.save(entity)
        eventPublisher.publishEvent(ProductIndexEvent(saved))
        return saved.toResponse()
    }

    @Transactional
    fun update(id: Long, entity: Product): ProductResponse {
        val existing = productRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Product not found: $id") }

        val updated = existing.copy(
            name = entity.name,
            description = entity.description,
            brand = entity.brand,
            category = entity.category,
            price = entity.price,
            quantity = entity.quantity,
            currency = entity.currency
        )

        val saved = productRepository.save(updated)
        eventPublisher.publishEvent(ProductIndexEvent(saved))
        return saved.toResponse()
    }

    @Transactional(readOnly = true)
    fun getBySku(sku: String): ProductResponse {
        val product = productRepository.findBySku(sku)
            ?: throw EntityNotFoundException("Product not found: $sku")
        return product.toResponse()
    }

    @Transactional
    fun updateQuantity(sku: String, delta: Long) {
        val product = productRepository.findBySku(sku)
            ?: throw EntityNotFoundException("Product not found: $sku")

        val newQuantity = product.quantity + delta

        if (newQuantity < 0) {
            throw QuantityUpdateException(
                "Quantity cannot become negative (current=${product.quantity}, delta=$delta)"
            )
        }

        product.quantity = newQuantity

        val saved = productRepository.save(product)
        eventPublisher.publishEvent(ProductIndexEvent(saved))
    }

    @Transactional
    fun updatePrice(sku: String, newPrice: BigDecimal) {
        val product = productRepository.findBySku(sku)
            ?: throw EntityNotFoundException("Product not found: $sku")

        product.price = newPrice.setScale(2, RoundingMode.HALF_UP)
        product.lastSyncedAt = Instant.now()

        val saved = productRepository.save(product)
        eventPublisher.publishEvent(ProductIndexEvent(saved))
    }

    @Transactional(readOnly = true)
    fun list(pageable: Pageable): Page<ProductResponse> =
        productRepository.findAll(pageable).map { it.toResponse() }

    @Transactional
    fun deleteProduct(sku: String) {
        val product = productRepository.findBySku(sku)
            ?: throw EntityNotFoundException("Product not found: $sku")

        productRepository.delete(product)
        eventPublisher.publishEvent(ProductDeleteEvent(sku))
    }

    @Transactional(readOnly = true)
    fun search(query: String, pageable: Pageable): Page<ProductSearchResponse> {

        val response = es.search<ProductDocument>({ s ->
            s.index("products")
                .query { q ->
                    q.multiMatch { mm ->
                        mm.query(query)
                            .fields("name", "description", "brand", "category")
                            .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                            .fuzziness("AUTO")
                    }
                }
                .from(pageable.offset.toInt())
                .size(pageable.pageSize)
        }, ProductDocument::class.java)


        val hits = response.hits().hits()

        val content = hits.map { hit ->
            val doc = hit.source()
            ProductSearchResponse(
                sku = doc!!.sku,
                name = doc.name,
                description = doc.description,
                brand = doc.brand,
                category = doc.category,
                price = doc.price,
                quantity = doc.quantity,
                currency = doc.currency,
                score = hit.score()?.toFloat()
            )
        }

        return org.springframework.data.domain.PageImpl(
            content,
            pageable,
            response.hits().total()?.value() ?: content.size.toLong()
        )
    }
}