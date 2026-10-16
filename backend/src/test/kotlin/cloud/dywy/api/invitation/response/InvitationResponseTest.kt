package cloud.dywy.api.invitation.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.api.invitation.response.InvitationResponseFixtures.brideFamily
import cloud.dywy.api.invitation.response.InvitationResponseFixtures.friends
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.friendsInvitation
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpDeclined
import cloud.dywy.domain.rsvp.entity.RsvpAttendance
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
    fun `should expose the guest attendance and an accepted summary`() {
        val response = brideFamilyInvitation.toResponse(mapOf(johnDoeRsvp.guestId to johnDoeRsvp))

        assertThat(response.guests.map { it.attendance }).isEqualTo(listOf(RsvpAttendance.ATTENDING))
        assertThat(response.rsvpSummary).isEqualTo(
            InvitationRsvpSummaryResponse(RsvpStatus.ACCEPTED_ALL, attending = 1, declined = 0, pending = 0, total = 1)
        )
    }

    @Test
    fun `should expose a declined summary when the guest declined`() {
        val response = brideFamilyInvitation.toResponse(mapOf(johnDoeRsvpDeclined.guestId to johnDoeRsvpDeclined))

        assertThat(response.guests.map { it.attendance }).isEqualTo(listOf(RsvpAttendance.DECLINED))
        assertThat(response.rsvpSummary).isEqualTo(
            InvitationRsvpSummaryResponse(RsvpStatus.DECLINED_ALL, attending = 0, declined = 1, pending = 0, total = 1)
        )
    }

    @Test
    fun `should mark the attendance as pending for a guest without rsvp`() {
        val response = friendsInvitation.toResponse(emptyMap())

        assertThat(response.guests.map { it.attendance }.distinct()).isEqualTo(listOf(RsvpAttendance.PENDING))
    }
}
