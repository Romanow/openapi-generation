package ru.romanow.openapi.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OpenApiServerApplication

fun main(args: Array<String>) {
    runApplication<OpenApiServerApplication>(*args)
}
