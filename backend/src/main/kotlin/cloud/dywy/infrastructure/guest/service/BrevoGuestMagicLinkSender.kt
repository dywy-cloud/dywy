package cloud.dywy.infrastructure.guest.service

import io.github.oshai.kotlinlogging.KotlinLogging
import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.GuestMagicLink
import cloud.dywy.domain.guest.service.GuestMagicLinkSender
import cloud.dywy.infrastructure.config.BrevoProperties
import cloud.dywy.infrastructure.config.GuestAccessProperties
import cloud.dywy.infrastructure.config.MailProperties
import cloud.dywy.infrastructure.shared.warnWithDetails
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException

private val logger = KotlinLogging.logger {}

/**
 * Sends the magic-link email through Brevo's HTTPS transactional API. Selected with
 * `app.mail.provider=brevo` (production on Render, where outbound SMTP is blocked). Best-effort:
 * a provider failure is logged and swallowed so the magic-link request always succeeds — the same
 * contract as [SmtpGuestMagicLinkSender].
 */
@Component
@ConditionalOnProperty(prefix = "app.mail", name = ["provider"], havingValue = "brevo")
class BrevoGuestMagicLinkSender(
    private val brevoApi: BrevoApi,
    private val guestAccessProperties: GuestAccessProperties,
    private val mailProperties: MailProperties,
    private val brevoProperties: BrevoProperties,
    private val guestMagicLinkEmailTemplate: GuestMagicLinkEmailTemplate,
) : GuestMagicLinkSender {

    override fun send(guestMagicLink: GuestMagicLink, guest: Guest) {
        val baseUrl = guestAccessProperties.baseUrl.trim().removeSuffix("/")
        val magicLinkUrl = "$baseUrl${guestMagicLink.guestAccessPath()}"

        val request = BrevoSendEmailRequest(
            sender = BrevoContact(email = mailProperties.from, name = brevoProperties.senderName),
            to = listOf(BrevoContact(email = guest.email, name = guest.firstName)),
            subject = guestMagicLinkEmailTemplate.subject(guest.language),
            htmlContent = guestMagicLinkEmailTemplate.htmlBody(guest.firstName, magicLinkUrl, guest.language),
            textContent = guestMagicLinkEmailTemplate.textBody(guest.firstName, magicLinkUrl, guest.language),
        )

        try {
            brevoApi.sendTransactionalEmail(request)
        } catch (e: RestClientException) {
            logger.warnWithDetails(e, "Failed to send magic-link email via Brevo") {
                "Failed to send magic-link email via Brevo (invitationId=${guestMagicLink.invitationId}, guestId=${guestMagicLink.guestId})"
            }
        }
    }
}

