package cloud.dywy.api.version

import cloud.dywy.AbstractEndpointIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import kotlin.test.Test

class VersionEndpointIT : AbstractEndpointIntegrationTest() {

    @Autowired
    private lateinit var buildProperties: BuildProperties

    @Test
    fun `should expose the build version without authentication`() {
        restTestClient.get().uri("/app/version")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.version").isEqualTo(buildProperties.version!!)
    }
}

