package nl.assignment.product.catalog.config

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ElasticsearchConfig {

    @Bean
    fun objectMapper(): ObjectMapper =
        ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Bean
    @ConditionalOnMissingBean
    fun elasticsearchClient(objectMapper: ObjectMapper): ElasticsearchClient {
        val restClient = RestClient.builder(
            HttpHost("elasticsearch", 9200, "http")
        ).build()

        val transport: ElasticsearchTransport = RestClientTransport(
            restClient,
            JacksonJsonpMapper(objectMapper)
        )

        return ElasticsearchClient(transport)
    }
}
