package cloud.dywy.infrastructure.guest.service

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.guest.entity.GuestFixtures.oliverBennett
import cloud.dywy.domain.guest.entity.GuestMagicLinkFixtures.bridesMaidToJane
import cloud.dywy.domain.guest.entity.Language
import org.springframework.context.support.ResourceBundleMessageSource
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestMagicLinkEmailTemplateTest {

    private val coupleDisplayName = "Thecla & Grégory"

    private lateinit var guestMagicLinkEmailTemplate: GuestMagicLinkEmailTemplate

    @BeforeTest
    fun setUp() {
        val messageSource = ResourceBundleMessageSource().apply {
            setBasename("messages")
            setDefaultEncoding("UTF-8")
            setFallbackToSystemLocale(false)
        }
        guestMagicLinkEmailTemplate = GuestMagicLinkEmailTemplate(messageSource)
    }

    @Test
    fun `should render a user friendly text body`() {
        val magicLinkUrl = "https://test.dywy.cloud${bridesMaidToJane.guestAccessPath()}"

        val textBody = guestMagicLinkEmailTemplate.textBody(janeDoe.firstName, magicLinkUrl, Language.FR)

        assertThat(textBody).all {
            contains("Bonjour ${janeDoe.firstName}")
            contains("lien sécurisé")
            contains("Nous sommes ravis de vous accueillir")
            contains(magicLinkUrl)
        }
    }

    @Test
    fun `should render html body with cta and fallback link`() {
        val magicLinkUrl = "https://test.dywy.cloud${bridesMaidToJane.guestAccessPath()}"

        val htmlBody = guestMagicLinkEmailTemplate.htmlBody(janeDoe.firstName, magicLinkUrl, Language.FR, coupleDisplayName)

        assertThat(htmlBody).all {
            contains("<html lang=\"fr\">")
            contains("Thecla &amp; Gr&eacute;gory")
            contains("Bonjour ${janeDoe.firstName}")
            contains("Accéder à mon invitation")
            contains("cid:${GuestMagicLinkEmailTemplate.ICON_CONTENT_ID}")
            contains(magicLinkUrl)
        }

    }

    @Test
    fun `should render an english text body when guest language is EN`() {
        val magicLinkUrl = "https://test.dywy.cloud${bridesMaidToJane.guestAccessPath()}"

        val textBody = guestMagicLinkEmailTemplate.textBody(oliverBennett.firstName, magicLinkUrl, Language.EN)

        assertThat(textBody).all {
            contains("Hello ${oliverBennett.firstName}")
            contains("secure link")
            contains(magicLinkUrl)
        }
    }

    @Test
    fun `should render an english html body when guest language is EN`() {
        val magicLinkUrl = "https://test.dywy.cloud${bridesMaidToJane.guestAccessPath()}"

        val htmlBody = guestMagicLinkEmailTemplate.htmlBody(oliverBennett.firstName, magicLinkUrl, Language.EN, coupleDisplayName)

        assertThat(htmlBody).all {
            contains("<html lang=\"en\">")
            contains("Hello ${oliverBennett.firstName}")
            contains("Access my invitation")
            contains(magicLinkUrl)
        }
    }

    @Test
    fun `should render a subject with the configured couple display name`() {
        assertThat(guestMagicLinkEmailTemplate.subject(Language.FR, coupleDisplayName))
            .contains(coupleDisplayName)
    }
}

