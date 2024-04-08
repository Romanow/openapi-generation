package ru.romanow.openapi.server.web

import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest
import ru.romanow.openapi.server.model.CreateServerRequest
import ru.romanow.openapi.server.model.ServerResponse
import ru.romanow.openapi.server.model.ServersResponse
import ru.romanow.openapi.server.service.ServerService

@RestController
class ServerController(
    private val serverService: ServerService
) : ApiController {

    override fun getById(id: Int): ResponseEntity<ServerResponse> = ok(serverService.getById(id))

    override fun all(): ResponseEntity<ServersResponse> = ok(ServersResponse(serverService.all()))

    override fun create(createServerRequest: CreateServerRequest): ResponseEntity<Unit> {
        val id = serverService.create(createServerRequest).id
        return ResponseEntity
            .created(
                fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(id)
                    .toUri()
            )
            .build()
    }

    override fun fullUpdate(id: Int, createServerRequest: CreateServerRequest): ResponseEntity<ServerResponse> =
        ok(serverService.update(id, createServerRequest))

    override fun partialUpdate(id: Int, createServerRequest: CreateServerRequest): ResponseEntity<ServerResponse> =
        ok(serverService.update(id, createServerRequest))

    override fun delete(id: Int): ResponseEntity<Unit> {
        serverService.delete(id)
        return noContent().build()
    }
}
