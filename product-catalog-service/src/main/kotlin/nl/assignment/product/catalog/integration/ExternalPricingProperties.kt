package nl.assignment.product.catalog.integration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "external.pricing")
class ExternalPricingProperties {
    lateinit var url: String
}