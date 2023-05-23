package ru.romanow.openapi.server.service

import ru.romanow.openapi.server.model.CreateServerRequest
import ru.romanow.openapi.server.model.ServerResponse

interface ServerService {
    fun getById(id: Int): ServerResponse
    fun all(): Array<ServerResponse>
    fun create(request: CreateServerRequest): ServerResponse
    fun delete(id: Int)
    fun update(id: Int, request: CreateServerRequest): ServerResponse
}