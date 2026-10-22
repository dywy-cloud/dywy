package cloud.dywy.application.rsvp

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommandFixtures.sofiaMartinAttending
import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommandFixtures.johnDoeDeclined
import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommandFixtures.lucasMartinDeclined
import cloud.dywy.application.rsvp.result.SubmitGuestRsvpResult
import cloud.dywy.application.rsvp.result.SubmitInvitationRsvpsResult
import cloud.dywy.domain.invitation.entity.InvitationFixtures.martinFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.invitation.repository.Invitations
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.sofiaMartinRsvp
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.lucasMartinRsvpDeclined
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlin.test.BeforeTest
import kotlin.test.Test

class InvitationRsvpSubmitterTest {

    private lateinit var invitations: Invitations
    private lateinit var guestRsvpSubmitter: GuestRsvpSubmitter
    private lateinit var invitationRsvpSubmitter: InvitationRsvpSubmitter

    @BeforeTest
    fun setUp() {
        invitations = mockk()
        guestRsvpSubmitter = mockk()
        invitationRsvpSubmitter = InvitationRsvpSubmitter(invitations, guestRsvpSubmitter)
        every { invitations.findById(martinFamilyInvitation.id) } returns martinFamilyInvitation
    }

    @Test
    fun `should submit the rsvp of every guest of the invitation`() {
        every { guestRsvpSubmitter.submit(sofiaMartinAttending) } returns SubmitGuestRsvpResult.Created(sofiaMartinRsvp)
        every { guestRsvpSubmitter.submit(lucasMartinDeclined) } returns SubmitGuestRsvpResult.Updated(lucasMartinRsvpDeclined)

        val result = invitationRsvpSubmitter.submit(martinFamilyInvitation.id, listOf(sofiaMartinAttending, lucasMartinDeclined))

        assertThat(result).isEqualTo(SubmitInvitationRsvpsResult.Submitted(listOf(sofiaMartinRsvp, lucasMartinRsvpDeclined)))
    }

    @Test
    fun `should save the guests in a stable order whatever the submission order`() {
        every { guestRsvpSubmitter.submit(any()) } returns SubmitGuestRsvpResult.Created(sofiaMartinRsvp)

        invitationRsvpSubmitter.submit(martinFamilyInvitation.id, listOf(lucasMartinDeclined, sofiaMartinAttending))

        verifyOrder {
            guestRsvpSubmitter.submit(sofiaMartinAttending)
            guestRsvpSubmitter.submit(lucasMartinDeclined)
        }
    }

    @Test
    fun `should reject a guest outside the invitation`() {
        val result = invitationRsvpSubmitter.submit(martinFamilyInvitation.id, listOf(sofiaMartinAttending, johnDoeDeclined))

        assertThat(result).isEqualTo(SubmitInvitationRsvpsResult.GuestNotInInvitation)
    }

    @Test
    fun `should save nothing when a guest is outside the invitation`() {
        invitationRsvpSubmitter.submit(martinFamilyInvitation.id, listOf(sofiaMartinAttending, johnDoeDeclined))

        verify(exactly = 0) { guestRsvpSubmitter.submit(any()) }
    }

    @Test
    fun `should report an unknown invitation`() {
        val unknownInvitationId = InvitationId.generate()
        every { invitations.findById(unknownInvitationId) } returns null

        val result = invitationRsvpSubmitter.submit(unknownInvitationId, listOf(sofiaMartinAttending))

        assertThat(result).isEqualTo(SubmitInvitationRsvpsResult.InvitationNotFound)
    }
}

