package nl.assignment.product.catalog.search

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface ProductSearchRepository : ElasticsearchRepository<ProductDocument, String> {

    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<ProductDocument>
}
