package nl.assignment.pricingmock

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

@RestController
class PricingMockController {

    @GetMapping("/mock-prices/{sku}")
    fun getPrice(
        @PathVariable sku: String,
        @RequestParam basePrice: BigDecimal
    ): ExternalPriceDto {
        val newPrice = (basePrice + BigDecimal.valueOf(Random.nextDouble(-5.0, 5.0)))
            .setScale(2, RoundingMode.HALF_UP)
        return ExternalPriceDto(sku, newPrice)
    }
}

data class ExternalPriceDto(
    val sku: String,
    val price: BigDecimal
)