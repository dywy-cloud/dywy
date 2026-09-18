package cloud.dywy.infrastructure.guest.service

import io.github.oshai.kotlinlogging.KotlinLogging
import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.GuestMagicLink
import cloud.dywy.domain.guest.service.GuestMagicLinkSender
import cloud.dywy.infrastructure.config.GuestAccessProperties
import cloud.dywy.infrastructure.config.MailProperties
import cloud.dywy.infrastructure.shared.warnWithDetails
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(prefix = "app.mail", name = ["provider"], havingValue = "smtp", matchIfMissing = true)
class SmtpGuestMagicLinkSender(
    private val javaMailSender: JavaMailSender,
    private val guestAccessProperties: GuestAccessProperties,
    private val mailProperties: MailProperties,
    private val guestMagicLinkEmailTemplate: GuestMagicLinkEmailTemplate,
) : GuestMagicLinkSender {

    override fun send(guestMagicLink: GuestMagicLink, guest: Guest) {
        val baseUrl = guestAccessProperties.baseUrl.trim().removeSuffix("/")
        val magicLinkUrl = "$baseUrl${guestMagicLink.guestAccessPath()}"

        val message = javaMailSender.createMimeMessage()
        MimeMessageHelper(message, true, Charsets.UTF_8.name()).apply {
            setFrom(mailProperties.from)
            setTo(guest.email)
            setSubject(guestMagicLinkEmailTemplate.subject(guest.language))
            setText(
                guestMagicLinkEmailTemplate.textBody(guestFirstName = guest.firstName, magicLinkUrl = magicLinkUrl, language = guest.language),
                guestMagicLinkEmailTemplate.htmlBody(guestFirstName = guest.firstName, magicLinkUrl = magicLinkUrl, language = guest.language),
            )
        }

        runCatching { javaMailSender.send(message) }
            .onFailure { error ->
                if (error is MailException) {
                    logger.warnWithDetails(error, "Failed to send magic-link email") {
                        "Failed to send magic-link email (invitationId=${guestMagicLink.invitationId}, guestId=${guestMagicLink.guestId})"
                    }
                    return
                }
                throw error
            }
    }

}
