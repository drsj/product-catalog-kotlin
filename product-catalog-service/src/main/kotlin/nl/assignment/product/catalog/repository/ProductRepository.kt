package nl.assignment.product.catalog.repository


import nl.assignment.product.catalog.domain.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {

    fun findBySku(sku: String): Product?

    fun deleteBySku(sku: String)

    fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Product>
}
