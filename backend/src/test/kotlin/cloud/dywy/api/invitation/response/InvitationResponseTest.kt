package cloud.dywy.api.invitation.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.api.invitation.response.InvitationResponseFixtures.brideFamily
import cloud.dywy.api.invitation.response.InvitationResponseFixtures.friends
import cloud.dywy.api.invitation.response.InvitationResponseFixtures.pendingGuestRsvpSummary
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.friendsInvitation
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpDeclined
import cloud.dywy.domain.rsvp.entity.RsvpStatus
import kotlin.test.Test

class InvitationResponseTest {

    @Test
    fun `should map invitation to response`() {
        val response = brideFamilyInvitation.toResponse(emptyMap())

        assertThat(response).isEqualTo(brideFamily)
    }

    @Test
    fun `should sort guests by id when mapping invitation`() {
        val response = friendsInvitation.toResponse(emptyMap())

        assertThat(response).isEqualTo(friends)
    }

    @Test
    fun `should map delivery method when invitation is posted`() {
        val response = friendsInvitation.copy(deliveryMethod = DeliveryMethod.POSTED).toResponse(emptyMap())

        assertThat(response.deliveryMethod).isEqualTo(DeliveryMethod.POSTED)
    }

    @Test
    fun `should expose an accepted summary for the invitation and the guest`() {
        val response = brideFamilyInvitation.toResponse(mapOf(johnDoeRsvp.guestId to johnDoeRsvp))
        val accepted = InvitationRsvpSummaryResponse(RsvpStatus.ACCEPTED_ALL, attending = 1, declined = 0, pending = 0, total = 1)

        assertThat(response.guests.map { it.rsvpSummary }).isEqualTo(listOf(accepted))
        assertThat(response.rsvpSummary).isEqualTo(accepted)
    }

    @Test
    fun `should expose a declined summary when the guest declined`() {
        val response = brideFamilyInvitation.toResponse(mapOf(johnDoeRsvpDeclined.guestId to johnDoeRsvpDeclined))
        val declined = InvitationRsvpSummaryResponse(RsvpStatus.DECLINED_ALL, attending = 0, declined = 1, pending = 0, total = 1)

        assertThat(response.guests.map { it.rsvpSummary }).isEqualTo(listOf(declined))
        assertThat(response.rsvpSummary).isEqualTo(declined)
    }

    @Test
    fun `should expose a pending summary for a guest without rsvp`() {
        val response = friendsInvitation.toResponse(emptyMap())

        assertThat(response.guests.map { it.rsvpSummary }.distinct()).isEqualTo(listOf(pendingGuestRsvpSummary))
    }
}
