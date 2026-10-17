package cloud.dywy.infrastructure.config

import cloud.dywy.AbstractEndpointIntegrationTest
import org.springframework.boot.test.web.server.LocalManagementPort
import org.springframework.test.web.servlet.client.RestTestClient
import kotlin.test.Test

class ActuatorHealthIT : AbstractEndpointIntegrationTest() {

    @LocalManagementPort
    private var managementPort: Int = 0

    private val managementClient: RestTestClient
        get() = RestTestClient.bindToServer().baseUrl("http://localhost:$managementPort").build()

    @Test
    fun `should report readiness as up on the management port`() {
        managementClient.get().uri("/actuator/health/readiness")
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.status").isEqualTo("UP")
    }

    @Test
    fun `should report liveness as up on the management port`() {
        managementClient.get().uri("/actuator/health/liveness")
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.status").isEqualTo("UP")
    }

    @Test
    fun `should hide health details`() {
        managementClient.get().uri("/actuator/health")
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.components").doesNotExist()
    }

    @Test
    fun `should not expose other actuator endpoints`() {
        listOf("env", "beans", "metrics", "info").forEach { endpoint ->
            managementClient.get().uri("/actuator/$endpoint")
                .exchange()
                .expectStatus().isNotFound
        }
    }

    @Test
    fun `should not serve actuator on the main port`() {
        restTestClient.get().uri("/actuator/health/readiness")
            .exchange()
            .expectStatus().isNotFound
    }
}

