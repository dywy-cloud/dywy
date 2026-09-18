package cloud.dywy.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.brevo")
data class BrevoProperties(
    val apiKey: String,
    val baseUrl: String = "https://api.brevo.com",
    val senderName: String = "DYWY",
)

