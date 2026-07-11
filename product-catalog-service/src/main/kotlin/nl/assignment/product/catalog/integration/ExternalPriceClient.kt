package nl.assignment.product.catalog.integration

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal

@Component
class ExternalPriceClient(
    builder: WebClient.Builder,
    private val props: ExternalPricingProperties
) {
    private val client = builder.baseUrl(props.url).build()

    fun getPrice(sku: String, currentPrice: BigDecimal): ExternalPriceDto? =
        client.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/mock-prices/{sku}")
                    .queryParam("basePrice", currentPrice)
                    .build(sku)
            }
            .retrieve()
            .bodyToMono(ExternalPriceDto::class.java)
            .timeout(props.timeout)
            .block()
}