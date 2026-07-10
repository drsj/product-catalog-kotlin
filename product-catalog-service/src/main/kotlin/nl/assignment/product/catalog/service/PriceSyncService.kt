package nl.assignment.product.catalog.service

import nl.assignment.product.catalog.integration.ExternalPriceClient
import nl.assignment.product.catalog.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.Executors

@Service
class PriceSyncService(
    private val productRepo: ProductRepository,
    private val externalClient: ExternalPriceClient,
    private val productService: ProductService
) {

    private val log = LoggerFactory.getLogger(PriceSyncService::class.java)

    @Scheduled(fixedDelayString = "\${pricing.sync.interval-ms:60000}")
    fun syncPrices() {
        val start = System.nanoTime()
        val products = productRepo.findAll()

        try {
            log.info("Starting price sync for ${products.size} products...")

            Executors.newVirtualThreadPerTaskExecutor().use { executor ->
                products.forEach { product ->
                    executor.submit { syncPrice(product.sku, product.price) }
                }
            }

            val durationSec = (System.nanoTime() - start) / 1_000_000_000.0
            log.info("Price sync completed in $durationSec seconds")

        } catch (e: Exception) {
            val durationSec = (System.nanoTime() - start) / 1_000_000_000.0
            log.error("Price sync failed after $durationSec seconds", e)
        }
    }

    private fun syncPrice(sku: String, currentPrice: java.math.BigDecimal) {
        val dto = externalClient.getPrice(sku, currentPrice)
        if (dto?.price != null) {
            productService.updatePrice(sku, dto.price)
        }
    }
}