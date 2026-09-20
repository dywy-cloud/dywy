package cloud.dywy.infrastructure.guest.service

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.mail.BodyPart
import jakarta.mail.Message
import jakarta.mail.Part
import jakarta.mail.Session
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.guest.entity.GuestMagicLinkFixtures.bridesMaidToJane
import cloud.dywy.infrastructure.config.GuestAccessPropertiesFixtures.testGuestAccessProperties
import cloud.dywy.infrastructure.config.MailProperties
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.context.support.ResourceBundleMessageSource
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class SmtpGuestMagicLinkSenderTest {

    private lateinit var javaMailSender: JavaMailSender
    private lateinit var smtpGuestMagicLinkSender: SmtpGuestMagicLinkSender

    @BeforeTest
    fun setUp() {
        javaMailSender = mockk(relaxed = true)
        val messageSource = ResourceBundleMessageSource().apply {
            setBasename("messages")
            setDefaultEncoding("UTF-8")
            setFallbackToSystemLocale(false)
        }
        smtpGuestMagicLinkSender = SmtpGuestMagicLinkSender(
            javaMailSender = javaMailSender,
            guestAccessProperties = testGuestAccessProperties.copy(baseUrl = "https://public.theweddingplan.app"),
            mailProperties = MailProperties(from = "no-reply@theweddingplan.app", coupleDisplayName = "Thecla & Grégory"),
            guestMagicLinkEmailTemplate = GuestMagicLinkEmailTemplate(messageSource),
        )
    }

    @Test
    fun `should send magic-link email with expected recipient and content`() {
        val messageSlot = slot<MimeMessage>()
        every { javaMailSender.createMimeMessage() } returns MimeMessage(Session.getInstance(Properties()))
        every { javaMailSender.send(capture(messageSlot)) } returns Unit

        smtpGuestMagicLinkSender.send(bridesMaidToJane, janeDoe)

        val sentMessage = messageSlot.captured
        sentMessage.saveChanges()
        val bodyContent = flattenMimeContent(sentMessage.content)

        verify(exactly = 1) { javaMailSender.send(any<MimeMessage>()) }
        assertThat(sentMessage.from.map { it.toString() }).isEqualTo(listOf("no-reply@theweddingplan.app"))
        assertThat(sentMessage.getRecipients(Message.RecipientType.TO).map { it.toString() })
            .isEqualTo(listOf(janeDoe.email))
        assertThat(sentMessage.subject).isEqualTo("dywy : Votre invitation au mariage de Thecla & Grégory")
        assertThat(bodyContent)
            .contains("Bonjour Jane")
        assertThat(bodyContent)
            .contains("https://public.theweddingplan.app/api/guest-access/magic-links/53c2efcd-b4fc-42f3-a73b-fadf3725af3f")
        assertThat(bodyContent)
            .contains("Accéder à mon invitation")
        assertThat(findBodyPartByContentId(sentMessage, GuestMagicLinkEmailTemplate.ICON_CONTENT_ID)?.contentType.orEmpty())
            .contains(GuestMagicLinkEmailAssets.ICON_CONTENT_TYPE)
    }

    @Test
    fun `should swallow mail send exception`() {
        every { javaMailSender.createMimeMessage() } returns MimeMessage(Session.getInstance(Properties()))
        every { javaMailSender.send(any<MimeMessage>()) } throws MailSendException("smtp down")

        smtpGuestMagicLinkSender.send(bridesMaidToJane, janeDoe)

        verify(exactly = 1) { javaMailSender.send(any<MimeMessage>()) }
    }

    private fun flattenMimeContent(content: Any): String = when (content) {
        is String -> content
        is MimeMultipart -> (0 until content.count)
            .joinToString("\n") { index -> flattenMimeContent(content.getBodyPart(index).content) }
        else -> content.toString()
    }

    private fun findBodyPartByContentId(part: Part, contentId: String): BodyPart? {
        val content = part.content
        if (content !is MimeMultipart) {
            return null
        }

        for (index in 0 until content.count) {
            val bodyPart = content.getBodyPart(index)
            if ((bodyPart as? MimeBodyPart)?.contentID?.trim('<', '>') == contentId) {
                return bodyPart
            }

            findBodyPartByContentId(bodyPart, contentId)?.let { return it }
        }

        return null
    }
}
