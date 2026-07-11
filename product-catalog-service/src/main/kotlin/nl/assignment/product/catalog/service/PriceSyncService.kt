package nl.assignment.product.catalog.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import nl.assignment.product.catalog.integration.ExternalPriceClient
import nl.assignment.product.catalog.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

            runBlocking(Dispatchers.IO) {
                supervisorScope {
                    products.forEach { product ->
                        launch {
                            try {
                                syncPrice(product.sku, product.price)
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                log.error("Price sync failed for SKU {}", product.sku, e)
                            }
                        }
                    }
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