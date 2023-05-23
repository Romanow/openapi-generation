package ru.romanow.openapi.client.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.romanow.openapi.client.config.properties.ServiceUrlProperties
import ru.romanow.openapi.client.model.ErrorResponse
import ru.romanow.openapi.client.model.ValidationErrorResponse
import ru.romanow.openapi.client.utils.prettyPrint

@Configuration
@EnableConfigurationProperties(ServiceUrlProperties::class)
class WebConfiguration {

    @Bean
    fun webClient(properties: ServiceUrlProperties): WebClient =
        WebClient
            .builder()
            .baseUrl(properties.serverUrl)
            .filter(ExchangeFilterFunction.ofResponseProcessor { exchangeFilterResponseProcessor(it) })
            .build()

    private fun exchangeFilterResponseProcessor(response: ClientResponse) =
        when (response.statusCode()) {
            HttpStatus.NOT_FOUND -> response.bodyToMono(String::class.java)
                .flatMap { Mono.error(RuntimeException(prettyPrint(it, ErrorResponse::class.java))) }

            HttpStatus.BAD_REQUEST -> response.bodyToMono(String::class.java)
                .flatMap { Mono.error(RuntimeException(prettyPrint(it, ValidationErrorResponse::class.java))) }

            else -> Mono.just(response)
        }
}