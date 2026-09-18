package cloud.dywy.application.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import cloud.dywy.application.guest.command.RequestGuestMagicLinkCommandFixtures.guestNotInInvitation
import cloud.dywy.application.guest.command.RequestGuestMagicLinkCommandFixtures.unknownInvitationForJaneDoe
import cloud.dywy.application.guest.command.RequestGuestMagicLinkCommandFixtures.validJaneDoe
import cloud.dywy.application.guest.result.RequestGuestMagicLinkResult
import cloud.dywy.application.invitation.InvitationTokenResolver
import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.GuestFixtures
import cloud.dywy.domain.guest.entity.GuestMagicLink
import cloud.dywy.domain.guest.repository.GuestMagicLinkTokens
import cloud.dywy.domain.guest.service.GuestMagicLinkSender
import cloud.dywy.domain.invitation.entity.InvitationFixtures
import cloud.dywy.domain.shared.Dates
import cloud.dywy.infrastructure.config.GuestAccessPropertiesFixtures.testGuestAccessProperties
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestMagicLinkRequesterTest {

    private lateinit var invitationTokenResolver: InvitationTokenResolver
    private lateinit var guestMagicLinkTokens: GuestMagicLinkTokens
    private lateinit var guestMagicLinkSender: GuestMagicLinkSender
    private lateinit var guestMagicLinkRequester: GuestMagicLinkRequester

    @BeforeTest
    fun setUp() {
        invitationTokenResolver = mockk()
        guestMagicLinkTokens = mockk(relaxed = true)
        guestMagicLinkSender = mockk(relaxed = true)
        guestMagicLinkRequester = GuestMagicLinkRequester(
            invitationTokenResolver = invitationTokenResolver,
            guestMagicLinkTokens = guestMagicLinkTokens,
            guestMagicLinkSender = guestMagicLinkSender,
            guestAccessProperties = testGuestAccessProperties,
        )
    }

    @Test
    fun `should send magic link when invitation token and selected guest are valid`() {
        every { invitationTokenResolver.resolve(InvitationFixtures.bridesMaidInvitation.accessToken) } returns InvitationFixtures.bridesMaidInvitation

        val guestMagicLink = slot<GuestMagicLink>()
        val guest = slot<Guest>()
        val result = guestMagicLinkRequester.request(validJaneDoe)

        verify(exactly = 1) { guestMagicLinkTokens.create(any()) }
        verify(exactly = 1) { guestMagicLinkSender.send(capture(guestMagicLink), capture(guest)) }
        assertThat(result).isEqualTo(RequestGuestMagicLinkResult.Sent)
        assertThat(guestMagicLink.captured.invitationId).isEqualTo(InvitationFixtures.bridesMaidInvitation.id)
        assertThat(guestMagicLink.captured.guestId).isEqualTo(GuestFixtures.janeDoe.id)
        assertThat(guest.captured.email).isEqualTo(GuestFixtures.janeDoe.email)
        assertThat(guest.captured.firstName).isEqualTo(GuestFixtures.janeDoe.firstName)
        assertThat(guestMagicLink.captured.token.value.isNotBlank()).isEqualTo(true)
        assertThat(guestMagicLink.captured.expiresAt).isGreaterThan(Dates.nowUtc())
    }

    @Test
    fun `should not send magic link when invitation token is unknown`() {
        every { invitationTokenResolver.resolve(unknownInvitationForJaneDoe.invitationAccessToken) } returns null

        val result = guestMagicLinkRequester.request(unknownInvitationForJaneDoe)

        verify(exactly = 0) { guestMagicLinkSender.send(any(), any()) }
        assertThat(result).isEqualTo(RequestGuestMagicLinkResult.InvitationNotFound)
    }

    @Test
    fun `should not send magic link when selected guest does not belong to invitation`() {
        every { invitationTokenResolver.resolve(InvitationFixtures.bridesMaidInvitation.accessToken) } returns InvitationFixtures.bridesMaidInvitation

        val result = guestMagicLinkRequester.request(guestNotInInvitation)

        verify(exactly = 0) { guestMagicLinkSender.send(any(), any()) }
        assertThat(result).isEqualTo(RequestGuestMagicLinkResult.GuestNotFound)
    }

    @Test
    fun `should not throw when sender fails for valid request`() {
        every { invitationTokenResolver.resolve(InvitationFixtures.bridesMaidInvitation.accessToken) } returns InvitationFixtures.bridesMaidInvitation
        every { guestMagicLinkSender.send(any(), any()) } throws IllegalStateException("smtp failure")

        val result = guestMagicLinkRequester.request(validJaneDoe)

        verify(exactly = 1) { guestMagicLinkSender.send(any(), any()) }
        assertThat(result).isEqualTo(RequestGuestMagicLinkResult.DeliveryFailed)
    }
}