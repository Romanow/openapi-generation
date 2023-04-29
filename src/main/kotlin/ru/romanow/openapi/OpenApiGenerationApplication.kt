package ru.romanow.openapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OpenApiGenerationApplication

fun main(args: Array<String>) {
    runApplication<OpenApiGenerationApplication>(*args)
}