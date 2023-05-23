package ru.romanow.openapi.server.model

data class ErrorDescription(
    val field: String,
    val error: String
)