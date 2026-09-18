package cloud.dywy.application.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import cloud.dywy.application.guest.result.GuestSessionResult
import cloud.dywy.domain.guest.entity.GuestFixtures.albertEinstein
import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.guest.entity.GuestSession
import cloud.dywy.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.nonExistingInvitationId
import cloud.dywy.domain.invitation.repository.Invitations
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestSessionResolverTest {

    private lateinit var invitations: Invitations
    private lateinit var guestSessionResolver: GuestSessionResolver

    @BeforeTest
    fun setUp() {
        invitations = mockk()
        guestSessionResolver = GuestSessionResolver(invitations)
    }

    @Test
    fun `should resolve the guest belonging to the invitation`() {
        every { invitations.findById(bridesMaidInvitation.id) } returns bridesMaidInvitation

        val result = guestSessionResolver.resolve(GuestSession(guestId = janeDoe.id, invitationId = bridesMaidInvitation.id))

        assertThat(result).isEqualTo(GuestSessionResult.Resolved(janeDoe))
    }

    @Test
    fun `should report guest not in invitation when the guest does not belong to it`() {
        every { invitations.findById(bridesMaidInvitation.id) } returns bridesMaidInvitation

        val result = guestSessionResolver.resolve(GuestSession(guestId = albertEinstein.id, invitationId = bridesMaidInvitation.id))

        assertThat(result).isEqualTo(GuestSessionResult.GuestNotInInvitation)
    }

    @Test
    fun `should report invitation not found when the invitation is missing`() {
        every { invitations.findById(nonExistingInvitationId) } returns null

        val result = guestSessionResolver.resolve(GuestSession(guestId = janeDoe.id, invitationId = nonExistingInvitationId))

        assertThat(result).isEqualTo(GuestSessionResult.InvitationNotFound)
    }
}

