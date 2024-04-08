package ru.romanow.openapi.test

import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.ObjectMapperConfig.objectMapperConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.filter.log.ErrorLoggingFilter
import org.apache.http.HttpHeaders.LOCATION
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
import ru.romanow.openapi.test.model.ServerResponse.PurposeEnum
import ru.romanow.openapi.test.model.ServerResponse.PurposeEnum.BACKEND
import ru.romanow.openapi.test.model.StateInfo
import ru.romanow.openapi.test.web.ServerApiApi
import java.util.function.BiPredicate

@TestMethodOrder(OrderAnnotation::class)
class ServerTest {

    private lateinit var api: ServerApiApi

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
            .serverApi()
    }

    @Test
    fun test() {
        var servers = api.all()
            .executeAs(validatedWith(shouldBeCode(SC_OK)))
            .servers

        val size = servers.size

        val request = buildCreateServerRequest()
        val location = api.create()
            .body(request)
            .execute(validatedWith(shouldBeCode(SC_CREATED)))
            .header(LOCATION)

        val serverId = location.substringAfterLast("/")
        assertThat(location).contains("/api/v1/servers/$serverId")

        val server = api.byId
            .idPath(serverId)
            .executeAs(validatedWith(shouldBeCode(SC_OK)))

        assertThat(server)
            .usingRecursiveComparison()
            .withEqualsForFields(BiPredicate { t: PurposeEnum, u: String -> t.name == u }, "purpose")
            .ignoringFields("id", "state.id")
            .isEqualTo(request)

        servers = api.all()
            .executeAs(validatedWith(shouldBeCode(SC_OK)))
            .servers

        assertThat(servers).hasSize(size + 1)

        api.delete()
            .idPath(serverId)
            .execute(validatedWith(shouldBeCode(SC_NO_CONTENT)))

        api.byId
            .idPath(serverId)
            .executeAs(validatedWith(shouldBeCode(SC_NOT_FOUND)))
    }

    private fun buildCreateServerRequest() =
        CreateServerRequest().also {
            it.bandwidth = 100
            it.latency = 100
            it.purpose = BACKEND.name
            it.state = StateInfo()
                .also { s ->
                    s.city = "Moscow"
                    s.country = "Russia"
                }
        }
}
