package cloud.dywy.infrastructure.config

import cloud.dywy.domain.guest.entity.Language
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.guest")
data class GuestProperties(
    val defaultLanguage: Language = Language.FR,
)

