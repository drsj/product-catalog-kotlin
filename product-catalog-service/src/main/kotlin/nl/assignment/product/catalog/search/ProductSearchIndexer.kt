package nl.assignment.product.catalog.search

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.transaction.event.TransactionPhase

@Component
class ProductSearchIndexer(
    private val es: ElasticsearchClient
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onProductIndexEvent(event: ProductIndexEvent) {
        val p = event.product

        es.index { i ->
            i.index("products")
                .id(p.sku)
                .document(
                    ProductDocument(
                        sku = p.sku,
                        name = p.name,
                        description = p.description,
                        brand = p.brand,
                        category = p.category,
                        price = p.price,
                        quantity = p.quantity,
                        currency = p.currency
                    )
                )
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onProductDeleteEvent(event: ProductDeleteEvent) {
        es.delete { d ->
            d.index("products")
                .id(event.sku)
        }
    }
}