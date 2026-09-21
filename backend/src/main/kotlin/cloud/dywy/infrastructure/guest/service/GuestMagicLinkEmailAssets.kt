package cloud.dywy.infrastructure.guest.service

import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ClassPathResource

object GuestMagicLinkEmailAssets {
    const val ICON_CONTENT_ID = "dywy-icon.png"
    const val ICON_CONTENT_TYPE = "image/png"
    const val PUBLIC_ICON_PATH = "/dywy-icon.png"

    private const val ICON_RESOURCE_PATH = "static/dywy-icon.png"

    private val iconBytes: ByteArray by lazy {
        ClassPathResource(ICON_RESOURCE_PATH).inputStream.use { it.readBytes() }
    }

    fun iconResource() = ByteArrayResource(iconBytes)

    fun publicIconUrl(baseUrl: String): String = "${baseUrl.trim().removeSuffix("/")}$PUBLIC_ICON_PATH"
}

