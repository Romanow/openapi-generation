package ru.romanow.openapi.test

import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.ObjectMapperConfig.objectMapperConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.filter.log.ErrorLoggingFilter
import org.apache.http.HttpHeaders
import org.apache.http.HttpStatus.SC_CREATED
import org.apache.http.HttpStatus.SC_NOT_FOUND
import org.apache.http.HttpStatus.SC_NO_CONTENT
import org.apache.http.HttpStatus.SC_OK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import ru.romanow.openapi.test.ApiClient.Config.apiConfig
import ru.romanow.openapi.test.JacksonObjectMapper.jackson
import ru.romanow.openapi.test.ResponseSpecBuilders.shouldBeCode
import ru.romanow.openapi.test.ResponseSpecBuilders.validatedWith
import ru.romanow.openapi.test.model.CreateServerRequest
import ru.romanow.openapi.test.model.Purpose
import ru.romanow.openapi.test.model.Purpose.BACKEND
import ru.romanow.openapi.test.model.Purpose.FRONTEND
import ru.romanow.openapi.test.model.StateInfo
import ru.romanow.openapi.test.web.ServerApi

/**
 * * Сделать несколько веток по повествованию.
 * * Сделать ветку с полностью описанным тестом.
 * * В `build.gradle` не писать код, а закомментировать.
 * * Написать requirements: Docker, Java.
 * * Описать шаблон на слайдах.
 * * Обновить QR codes.
 * * Добавить ссылку на проект перед live-coding.
 */
@TestMethodOrder(OrderAnnotation::class)
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
                    .addFilter(ErrorLoggingFilter())
                    .setBaseUri("http://localhost:8080")
            }
        )
            .server()
    }

    @Test
    fun test() {
        var servers = api.all()
            .executeAs(validatedWith(shouldBeCode(SC_OK)))
            .servers

        val initialSize = servers.size

        var request = buildCreateServerRequest()
        val location = api.create()
            .body(request)
            .execute(validatedWith(shouldBeCode(SC_CREATED)))
            .header(HttpHeaders.LOCATION)

        // Location: https://localhost:8080/api/v1/servers/{{serverId}}
        assertThat(location).contains("/api/v1/servers/")
        val serverId = location.substringAfterLast("/")

        var server = api.byId
            .idPath(serverId)
            .executeAs(validatedWith(shouldBeCode(SC_OK)))

        assertThat(server)
            .usingRecursiveComparison()
            .ignoringFields("id", "state.id")
            .isEqualTo(request)

        servers = api.all()
            .executeAs(validatedWith(shouldBeCode(SC_OK)))
            .servers

        assertThat(servers).hasSize(initialSize + 1)

        request = buildCreateServerRequest(purpose = FRONTEND)
        server = api.fullUpdate()
            .idPath(serverId)
            .body(request)
            .executeAs(validatedWith(shouldBeCode(SC_OK)))

        assertThat(server)
            .usingRecursiveComparison()
            .ignoringFields("id", "state.id")
            .isEqualTo(request)

        api.delete().idPath(serverId)
            .execute(validatedWith(shouldBeCode(SC_NO_CONTENT)))

        api.byId.idPath(serverId)
            .executeAs(validatedWith(shouldBeCode(SC_NOT_FOUND)))
    }

    private fun buildCreateServerRequest(purpose: Purpose = BACKEND) =
        CreateServerRequest().also {
            it.bandwidth = 100
            it.latency = 100
            it.purpose = purpose
            it.state = StateInfo()
                .also { s ->
                    s.city = "Moscow"
                    s.country = "Russia"
                }
        }
}
