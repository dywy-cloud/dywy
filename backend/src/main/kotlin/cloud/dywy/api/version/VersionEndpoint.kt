package cloud.dywy.api.version

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerResponse

@Component
class VersionEndpoint(
    private val buildProperties: ObjectProvider<BuildProperties>,
) {

    fun version(): ServerResponse =
        ServerResponse.ok().body(VersionResponse(buildProperties.ifAvailable?.version ?: UNKNOWN_VERSION))

    private companion object {
        const val UNKNOWN_VERSION = "unknown"
    }
}

data class VersionResponse(
    val version: String,
)

