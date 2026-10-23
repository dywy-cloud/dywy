package cloud.dywy.api.rsvp.response

import cloud.dywy.domain.rsvp.entity.InvitationGuestRsvp

data class InvitationRsvpsResponse(
    val guests: List<InvitationGuestRsvpResponse>,
)

data class InvitationGuestRsvpResponse(
    val guestId: String,
    val firstName: String,
    val rsvp: GuestRsvpResponse?,
)

internal fun List<InvitationGuestRsvp>.toInvitationResponse() = InvitationRsvpsResponse(
    guests = map {
        InvitationGuestRsvpResponse(
            guestId = it.guestId.toString(),
            firstName = it.firstName,
            rsvp = it.rsvp?.toResponse(),
        )
    },
)

