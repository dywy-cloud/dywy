package cloud.dywy.api.rsvp.response

import cloud.dywy.domain.guest.entity.GuestFixtures.lucasMartin
import cloud.dywy.domain.guest.entity.GuestFixtures.sofiaMartin

object InvitationRsvpsResponseFixtures {
    val martinFamilyUnanswered = InvitationRsvpsResponse(
        guests = listOf(
            InvitationGuestRsvpResponse(guestId = sofiaMartin.id.toString(), firstName = sofiaMartin.firstName, rsvp = null),
            InvitationGuestRsvpResponse(guestId = lucasMartin.id.toString(), firstName = lucasMartin.firstName, rsvp = null),
        ),
    )

    val sofiaAnsweredLucasPending = InvitationRsvpsResponse(
        guests = listOf(
            InvitationGuestRsvpResponse(
                guestId = sofiaMartin.id.toString(),
                firstName = sofiaMartin.firstName,
                rsvp = GuestRsvpResponse(
                    id = "019fb445-4209-75ad-9370-e16ff6140b39",
                    version = 1L,
                    creationDate = "2026-06-13T10:00",
                    updateDate = "2026-06-13T10:00",
                    attendance = "ATTENDING",
                    meal = "MEAT",
                    song = SongResponseFixtures.laVieEnRose,
                ),
            ),
            InvitationGuestRsvpResponse(guestId = lucasMartin.id.toString(), firstName = lucasMartin.firstName, rsvp = null),
        ),
    )
}

