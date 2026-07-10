package nl.assignment.product.catalog.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "products")
data class Product(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val sku: String = "",

    @Column(nullable = false)
    var name: String = "",

    @Column(length = 4000)
    var description: String? = null,

    var brand: String? = null,

    var category: String? = null,

    @Column(nullable = false, scale = 2, precision = 10)
    var price: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var quantity: Long = 0,

    var currency: String? = null,

    var lastSyncedAt: Instant? = null
)