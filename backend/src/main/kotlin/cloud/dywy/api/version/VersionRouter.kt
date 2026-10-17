package cloud.dywy.api.version

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.router

@Configuration
class VersionRouter(
    private val versionEndpoint: VersionEndpoint,
) {

    @Bean
    fun versionRoute() = router {
        GET("/app/version") { versionEndpoint.version() }
    }
}

