package cloud.dywy.infrastructure.config

import cloud.dywy.AbstractEndpointIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalManagementPort
import org.springframework.test.web.servlet.client.RestTestClient
import javax.sql.DataSource
import kotlin.test.Test

class ActuatorReadinessDatabaseDownIT : AbstractEndpointIntegrationTest() {

    @LocalManagementPort
    private var managementPort: Int = 0

    @Autowired
    private lateinit var dataSource: DataSource

    private val managementClient: RestTestClient
        get() = RestTestClient.bindToServer().baseUrl("http://localhost:$managementPort").build()

    @Test
    fun `should report readiness as down while liveness stays up when the database is unavailable`() {
        managementClient.get().uri("/actuator/health/readiness")
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.status").isEqualTo("UP")

        (dataSource as AutoCloseable).close()

        managementClient.get().uri("/actuator/health/readiness")
            .exchange()
            .expectStatus().isEqualTo(503)
            .expectBody().jsonPath("$.status").isEqualTo("DOWN")

        managementClient.get().uri("/actuator/health/liveness")
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.status").isEqualTo("UP")
    }
}

