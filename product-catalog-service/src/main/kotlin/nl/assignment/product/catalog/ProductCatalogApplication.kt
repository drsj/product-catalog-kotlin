package nl.assignment.product.catalog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaRepositories("nl.assignment.product.catalog.repository")
@EnableScheduling
class ProductCatalogApplication

fun main(args: Array<String>) {
    runApplication<ProductCatalogApplication>(*args)
}