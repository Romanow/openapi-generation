package ru.romanow.openapi.server.model

data class ValidationErrorResponse(
    val message: String,
    val error: List<ErrorDescription>
)
