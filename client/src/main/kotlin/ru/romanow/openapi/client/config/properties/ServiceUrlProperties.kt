package ru.romanow.openapi.client.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application.services")
data class ServiceUrlProperties(
    val serverUrl: String
)
