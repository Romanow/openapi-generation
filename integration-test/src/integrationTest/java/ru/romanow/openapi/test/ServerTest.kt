package ru.romanow.openapi.test

import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.ObjectMapperConfig.objectMapperConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import org.apache.http.HttpHeaders
import org.apache.http.HttpStatus.SC_CREATED
import org.apache.http.HttpStatus.SC_NOT_FOUND
import org.apache.http.HttpStatus.SC_NO_CONTENT
import org.apache.http.HttpStatus.SC_OK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.romanow.openapi.test.ApiClient.Config.apiConfig
import ru.romanow.openapi.test.JacksonObjectMapper.jackson
import ru.romanow.openapi.test.ResponseSpecBuilders.shouldBeCode
import ru.romanow.openapi.test.ResponseSpecBuilders.validatedWith
import ru.romanow.openapi.test.model.CreateServerRequest
import ru.romanow.openapi.test.model.Purpose
import ru.romanow.openapi.test.model.Purpose.BACKEND
import ru.romanow.openapi.test.model.StateInfo
import ru.romanow.openapi.test.web.ServerApi

/**
 * **Test Plan**
 * 1. Получить список всех серверов.
 * 2. Создать новый сервер, получить его Id.
 * 3. Получить новый сервер по Id, проверить его поля.
 * 4. Выполнить частичное обновление сервера (`purpose`).
 * 5. Выполнить полное обновление сервера (latency, bandwidth, state.city).
 * 6. Удалить ранее созданный сервер по Id.
 * 7. Проверить, что сервер действительно удален (статус 404 Not Found).
 */
class ServerTest {

    private lateinit var api: ServerApi

    @BeforeEach
    fun init() {
        api = ApiClient.api(
            apiConfig().reqSpecSupplier {
                RequestSpecBuilder()
                    .setConfig(
                        RestAssuredConfig.config().objectMapperConfig(
                            objectMapperConfig().defaultObjectMapper(jackson())
                        )
                    )
                    .addFilter(RequestLoggingFilter())
                    .addFilter(ResponseLoggingFilter())
                    .setBaseUri("http://localhost:8080")
            }
        )
            .server()
    }

    @Test
    fun test() {
        // Получить список всех серверов
        val servers = api.all()
            .executeAs(validatedWith(shouldBeCode(SC_OK)))
            .servers
        assertThat(servers).hasSizeGreaterThanOrEqualTo(0)

        // Создать новый сервер, получить его Id
        var request = buildCreateServerRequest()
        val location = api.create()
            .body(request)
            .execute(validatedWith(shouldBeCode(SC_CREATED)))
            .header(HttpHeaders.LOCATION)

        // Location: https://localhost:8080/api/v1/servers/{{serverId}}
        assertThat(location).contains("/api/v1/servers/")
        val serverId = location.substringAfterLast("/")

        // Получить новый сервер по Id, проверить его поля
        var server = api.byId
            .idPath(serverId)
            .executeAs(validatedWith(shouldBeCode(SC_OK)))

        assertThat(server)
            .usingRecursiveComparison()
            .ignoringFields("id", "state.id")
            .isEqualTo(request)

        // Выполнить частичное обновление сервера (purpose)
        request = CreateServerRequest().also { it.purpose = Purpose.FRONTEND }
        server = api.partialUpdate()
            .idPath(serverId)
            .body(request)
            .executeAs(validatedWith(shouldBeCode(SC_OK)))
        assertThat(server.purpose).isEqualTo(Purpose.FRONTEND)

        // Выполнить полное обновление сервера (latency, bandwidth, state.city)
        // Выполнить частичное обновление сервера (purpose)
        request = buildCreateServerRequest(latency = 80, bandwidth = 80, city = "Saint Petersburg")
        server = api.fullUpdate()
            .idPath(serverId)
            .body(request)
            .executeAs(validatedWith(shouldBeCode(SC_OK)))

        assertThat(server)
            .usingRecursiveComparison()
            .ignoringFields("id", "state.id")
            .isEqualTo(request)

        // Удалить ранее созданный сервер по Id
        api.delete().idPath(serverId)
            .execute(validatedWith(shouldBeCode(SC_NO_CONTENT)))

        // Проверить, что сервер действительно удален (статус 404 Not Found)
        api.byId.idPath(serverId)
            .executeAs(validatedWith(shouldBeCode(SC_NOT_FOUND)))
    }

    private fun buildCreateServerRequest(
        purpose: Purpose = BACKEND,
        bandwidth: Int = 100,
        latency: Int = 100,
        city: String = "Moscow",
        country: String = "Russia"
    ) =
        CreateServerRequest().also {
            it.bandwidth = bandwidth
            it.latency = latency
            it.purpose = purpose
            it.state = StateInfo().also { s ->
                s.city = city
                s.country = country
            }
        }
}
