package ru.romanow.openapi.client.service

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.toEntity
import reactor.core.publisher.Mono
import ru.romanow.openapi.client.models.CreateServerRequest
import ru.romanow.openapi.client.models.Purpose
import ru.romanow.openapi.client.models.ServerResponse
import ru.romanow.openapi.client.models.ServersResponse
import ru.romanow.openapi.client.models.StateInfo
import ru.romanow.openapi.client.utils.prettyPrint

@Service
class ServerClient(
    private val webClient: WebClient
) {

    fun create(purpose: String): String {
        val request = CreateServerRequest(
            purpose = Purpose.valueOf(purpose),
            latency = 10,
            bandwidth = 10000,
            state = StateInfo(city = "Moscow", country = "Russia")
        )

        val location = webClient.post()
            .uri("/api/v1/servers")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .toEntity<Void>()
            .map { it.headers.location }
            .block()!!

        return location.path.split("/").last()
    }

    fun findAll() =
        webClient.get()
            .uri("/api/v1/servers")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(ServersResponse::class.java)
            .map { prettyPrint(it) }
            .block()!!

    fun getById(id: Int) =
        webClient.get()
            .uri("/api/v1/servers/{id}", id)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(ServerResponse::class.java)
            .map { prettyPrint(it) }
            .onErrorResume { Mono.just(it.message!!) }
            .block()!!

    fun findInCity(city: String) =
        webClient.get()
            .uri { it.path("/api/v1/servers").queryParam("city", city).build() }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(ServersResponse::class.java)
            .map { prettyPrint(it) }
            .onErrorResume { Mono.just(it.message!!) }
            .block()!!

    fun update(
        id: Int, purpose: String?, latency: Int?, bandwidth: Int?, city: String?, country: String?
    ): String {
        val request = CreateServerRequest(
            purpose = if (purpose != null) Purpose.valueOf(purpose) else null,
            latency = latency,
            bandwidth = bandwidth,
            state = StateInfo(city = city, country = country)
        )

        return webClient
            .patch()
            .uri("/api/v1/servers/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .bodyToMono(ServerResponse::class.java)
            .map { prettyPrint(it) }
            .onErrorResume { Mono.just(it.message!!) }
            .block()!!
    }

    fun delete(id: Int) {
        webClient
            .delete()
            .uri("/api/v1/servers/{id}", id)
            .retrieve()
            .bodyToMono(Void::class.java)
            .block()
    }
}
