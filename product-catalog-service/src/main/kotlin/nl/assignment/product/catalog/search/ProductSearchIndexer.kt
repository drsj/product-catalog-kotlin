package nl.assignment.product.catalog.search

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ProductSearchIndexer(
    private val productSearchRepository: ProductSearchRepository
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onProductIndexEvent(event: ProductIndexEvent) {
        val product = event.product
        productSearchRepository.save(
            ProductDocument(
                sku = product.sku,
                name = product.name,
                description = product.description,
                brand = product.brand,
                category = product.category,
                price = product.price,
                quantity = product.quantity,
                currency = product.currency
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onProductDeleteEvent(event: ProductDeleteEvent) {
        productSearchRepository.deleteById(event.sku)
    }
}
