package ru.romanow.openapi.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OpenApiClientApplication

fun main(args: Array<String>) {
    runApplication<OpenApiClientApplication>(*args)
}