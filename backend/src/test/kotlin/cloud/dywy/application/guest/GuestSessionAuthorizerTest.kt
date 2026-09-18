package cloud.dywy.application.guest

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import io.mockk.every
import io.mockk.mockk
import cloud.dywy.domain.guest.entity.GuestFixtures.albertEinstein
import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.nonExistingInvitationId
import cloud.dywy.domain.invitation.repository.Invitations
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestSessionAuthorizerTest {

    private lateinit var invitations: Invitations
    private lateinit var guestSessionAuthorizer: GuestSessionAuthorizer

    @BeforeTest
    fun setUp() {
        invitations = mockk()
        guestSessionAuthorizer = GuestSessionAuthorizer(invitations)
    }

    @Test
    fun `should authorize guest belonging to invitation`() {
        every { invitations.findById(bridesMaidInvitation.id) } returns bridesMaidInvitation

        assertThat(guestSessionAuthorizer.isGuestInInvitation(bridesMaidInvitation.id, janeDoe.id)).isTrue()
    }

    @Test
    fun `should reject guest not belonging to invitation`() {
        every { invitations.findById(bridesMaidInvitation.id) } returns bridesMaidInvitation

        assertThat(guestSessionAuthorizer.isGuestInInvitation(bridesMaidInvitation.id, albertEinstein.id)).isFalse()
    }

    @Test
    fun `should reject when invitation is not found`() {
        every { invitations.findById(nonExistingInvitationId) } returns null

        assertThat(guestSessionAuthorizer.isGuestInInvitation(nonExistingInvitationId, janeDoe.id)).isFalse()
    }
}

