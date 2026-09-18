package cloud.dywy.application.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import cloud.dywy.application.guest.result.GuestMagicLinkVerificationResult
import cloud.dywy.domain.guest.entity.GuestFixtures.albertEinstein
import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.guest.entity.ConsumedGuestMagicLinkToken
import cloud.dywy.domain.guest.entity.GuestMagicLinkAccessToken
import cloud.dywy.domain.guest.repository.GuestMagicLinkTokens
import cloud.dywy.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import cloud.dywy.domain.invitation.repository.Invitations
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestMagicLinkVerifierTest {

    private lateinit var guestMagicLinkTokens: GuestMagicLinkTokens
    private lateinit var invitations: Invitations
    private lateinit var guestMagicLinkVerifier: GuestMagicLinkVerifier

    @BeforeTest
    fun setUp() {
        guestMagicLinkTokens = mockk()
        invitations = mockk()
        guestMagicLinkVerifier = GuestMagicLinkVerifier(guestMagicLinkTokens, invitations)
    }

    @Test
    fun `should verify and resolve invitation when token is valid`() {
        val token = GuestMagicLinkAccessToken.fromStringOrNull("53c2efcd-b4fc-42f3-a73b-fadf3725af3f")!!
        every { guestMagicLinkTokens.consumeIfValid(token, any()) } returns ConsumedGuestMagicLinkToken(
            invitationId = bridesMaidInvitation.id,
            guestId = janeDoe.id,
        )
        every { invitations.findById(bridesMaidInvitation.id) } returns bridesMaidInvitation

        val result = guestMagicLinkVerifier.verify(token)

        assertThat(result).isEqualTo(
            GuestMagicLinkVerificationResult.Verified(
                invitation = bridesMaidInvitation,
                guestId = janeDoe.id,
            )
        )
    }

    @Test
    fun `should return InvalidOrExpiredOrUsedToken when token is expired or already used`() {
        val token = GuestMagicLinkAccessToken.fromStringOrNull("53c2efcd-b4fc-42f3-a73b-fadf3725af3f")!!
        every { guestMagicLinkTokens.consumeIfValid(token, any()) } returns null

        val result = guestMagicLinkVerifier.verify(token)

        assertThat(result).isEqualTo(GuestMagicLinkVerificationResult.InvalidOrExpiredOrUsedToken)
    }

    @Test
    fun `should return GuestNotInInvitation when invitation does not contain token guest`() {
        val token = GuestMagicLinkAccessToken.fromStringOrNull("53c2efcd-b4fc-42f3-a73b-fadf3725af3f")!!
        every { guestMagicLinkTokens.consumeIfValid(token, any()) } returns ConsumedGuestMagicLinkToken(
            invitationId = bridesMaidInvitation.id,
            guestId = albertEinstein.id,
        )
        every { invitations.findById(bridesMaidInvitation.id) } returns bridesMaidInvitation

        val result = guestMagicLinkVerifier.verify(token)

        assertThat(result).isEqualTo(GuestMagicLinkVerificationResult.GuestNotInInvitation)
    }

    @Test
    fun `should return invitation not found when consumed token references missing invitation`() {
        val token = GuestMagicLinkAccessToken.fromStringOrNull("53c2efcd-b4fc-42f3-a73b-fadf3725af3f")!!
        every { guestMagicLinkTokens.consumeIfValid(token, any()) } returns ConsumedGuestMagicLinkToken(
            invitationId = bridesMaidInvitation.id,
            guestId = janeDoe.id,
        )
        every { invitations.findById(bridesMaidInvitation.id) } returns null

        val result = guestMagicLinkVerifier.verify(token)

        assertThat(result).isEqualTo(GuestMagicLinkVerificationResult.InvitationNotFound)
    }
}


