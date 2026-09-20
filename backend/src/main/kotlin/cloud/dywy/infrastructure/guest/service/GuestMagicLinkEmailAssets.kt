package cloud.dywy.infrastructure.guest.service

import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ClassPathResource
import java.util.Base64

object GuestMagicLinkEmailAssets {
    const val ICON_CONTENT_ID = "dywy-icon.svg"
    const val ICON_CONTENT_TYPE = "image/svg+xml"

    private const val ICON_RESOURCE_PATH = "mail/dywy-icon.svg"

    private val iconBytes: ByteArray by lazy {
        ClassPathResource(ICON_RESOURCE_PATH).inputStream.use { it.readBytes() }
    }

    fun iconResource() = ByteArrayResource(iconBytes)

    fun iconBase64Content(): String = Base64.getEncoder().encodeToString(iconBytes)

    fun iconDataUri(): String = "data:$ICON_CONTENT_TYPE;base64,${iconBase64Content()}"
}

