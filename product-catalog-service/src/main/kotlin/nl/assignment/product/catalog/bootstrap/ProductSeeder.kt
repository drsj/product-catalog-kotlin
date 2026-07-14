package nl.assignment.product.catalog.bootstrap

import nl.assignment.product.catalog.domain.Product
import nl.assignment.product.catalog.repository.ProductRepository
import nl.assignment.product.catalog.search.ProductIndexEvent
import nl.assignment.product.catalog.util.ProductUtil
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

@Component
class ProductSeeder(
    private val productRepository: ProductRepository,
    private val eventPublisher: ApplicationEventPublisher
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(ProductSeeder::class.java)

    @Transactional
    override fun run(vararg args: String) {
        val count = productRepository.count()

        if (count > 0) {
            logger.info("Skipping product seeding: $count products already exist")
            productRepository.findAll().forEach { product ->
                eventPublisher.publishEvent(ProductIndexEvent(product))
            }
            return
        }

        logger.info("Seeding 1000 products...")
        val products = (1..1000).map { generateRealisticProduct(it) }

        val savedProducts = productRepository.saveAll(products)
        savedProducts.forEach { product ->
            eventPublisher.publishEvent(ProductIndexEvent(product))
        }
        logger.info("Successfully seeded ${products.size} products")
    }

    private fun generateRealisticProduct(index: Int): Product {
        val brands = listOf(
            "Apple", "Samsung", "Sony", "LG", "Dell",
            "HP", "Lenovo", "Bose", "JBL", "Microsoft"
        )

        val categories = listOf(
            "mobile", "laptop", "audio", "tv",
            "tablet", "smartwatch", "speaker", "monitor"
        )

        val brand = brands.random()
        val category = categories.random()

        val name = generateName(brand, category)
        val description = "$brand $name"

        val sku = generateSku(brand, name, index)

        val price = BigDecimal.valueOf(
            Random.nextDouble(50.0, 2500.0)
        ).setScale(2, RoundingMode.HALF_UP)

        val quantity = Random.nextLong(0, 100)

        return ProductUtil.getProduct(
            sku = sku,
            name = name,
            description = description,
            brand = brand,
            category = category,
            price = price,
            quantity = quantity
        )
    }

    private fun generateName(brand: String, category: String): String =
        when (brand) {
            "Apple" -> when (category) {
                "mobile" -> "iPhone 15"
                "tablet" -> "iPad Pro 12.9"
                "laptop" -> "MacBook Air M3"
                "smartwatch" -> "Apple Watch Series 9"
                else -> "$brand $category"
            }
            "Samsung" -> when (category) {
                "mobile" -> "Galaxy S24"
                "tablet" -> "Galaxy Tab S9"
                "tv" -> "Samsung QLED 55\""
                "laptop" -> "Galaxy Book 4"
                else -> "$brand $category"
            }
            "Sony" -> when (category) {
                "audio" -> "WH‑1000XM5"
                "tv" -> "Bravia XR 65\""
                "speaker" -> "Sony SRS‑XB43"
                else -> "$brand $category"
            }
            "LG" -> when (category) {
                "tv" -> "LG OLED C3 55\""
                "monitor" -> "LG UltraFine 27\""
                else -> "$brand $category"
            }
            "Dell" -> "XPS 13"
            "HP" -> "Spectre x360"
            "Lenovo" -> "ThinkPad X1 Carbon"
            "Bose" -> "QuietComfort Ultra"
            "JBL" -> "Charge 5"
            "Microsoft" -> "Surface Laptop 6"
            else -> "$brand $category"
        }

    private fun generateSku(brand: String, name: String, index: Int): String {
        val brandCode = safeCode(brand)
        val nameCode = safeCode(name)
        return "$brandCode-$nameCode-$index"
    }

    private fun safeCode(input: String?): String {
        val cleaned = input?.replace(Regex("[^A-Za-z]"), "") ?: "UNK"

        return when {
            cleaned.length >= 3 -> cleaned.substring(0, 3).uppercase()
            else -> (cleaned + "XXX").substring(0, 3).uppercase()
        }
    }
}