package cloud.dywy.api.rsvp.request

import cloud.dywy.domain.guest.entity.GuestFixtures.sofiaMartin
import cloud.dywy.domain.guest.entity.GuestFixtures.johnDoe
import cloud.dywy.domain.guest.entity.GuestFixtures.lucasMartin

object SubmitInvitationRsvpsRequestFixtures {

    val sofiaAttending = InvitationGuestRsvpRequest(
        guestId = sofiaMartin.id.toString(),
        rsvp = SubmitRsvpRequestFixtures.attending,
    )

    val lucasDeclined = InvitationGuestRsvpRequest(
        guestId = lucasMartin.id.toString(),
        rsvp = SubmitRsvpRequestFixtures.declined,
    )

    val johnDoeDeclined = InvitationGuestRsvpRequest(
        guestId = johnDoe.id.toString(),
        rsvp = SubmitRsvpRequestFixtures.declined,
    )

    val sofiaAndLucas = SubmitInvitationRsvpsRequest(rsvps = listOf(sofiaAttending, lucasDeclined))

    val sofiaAndForeignGuest = SubmitInvitationRsvpsRequest(rsvps = listOf(sofiaAttending, johnDoeDeclined))

    val sofiaTwice = SubmitInvitationRsvpsRequest(rsvps = listOf(sofiaAttending, sofiaAttending))

    val sofiaWithoutMealAndLucas = SubmitInvitationRsvpsRequest(
        rsvps = listOf(sofiaAttending.copy(rsvp = SubmitRsvpRequestFixtures.attendingWithoutMeal), lucasDeclined),
    )

    val sofiaWithInvalidAttendanceAndLucas = SubmitInvitationRsvpsRequest(
        rsvps = listOf(sofiaAttending.copy(rsvp = SubmitRsvpRequestFixtures.invalidAttendance), lucasDeclined),
    )

    val malformedGuestId = SubmitInvitationRsvpsRequest(
        rsvps = listOf(sofiaAttending.copy(guestId = "not-a-uuid")),
    )

    val noRsvps = SubmitInvitationRsvpsRequest(rsvps = emptyList())

    val tooManyRsvps = SubmitInvitationRsvpsRequest(rsvps = List(51) { sofiaAttending })
}


