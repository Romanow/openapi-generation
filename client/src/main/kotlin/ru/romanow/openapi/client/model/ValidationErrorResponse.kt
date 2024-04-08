package ru.romanow.openapi.client.model

data class ValidationErrorResponse(
    val message: String,
    val error: List<ErrorDescription>
)
