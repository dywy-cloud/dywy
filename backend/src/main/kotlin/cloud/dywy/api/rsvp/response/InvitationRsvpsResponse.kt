package cloud.dywy.api.rsvp.response

import cloud.dywy.domain.rsvp.entity.GuestRsvp

data class InvitationRsvpsResponse(
    val rsvps: List<InvitationGuestRsvpResponse>,
)

data class InvitationGuestRsvpResponse(
    val guestId: String,
    val rsvp: GuestRsvpResponse,
)

internal fun List<GuestRsvp>.toInvitationResponse() = InvitationRsvpsResponse(
    rsvps = map { InvitationGuestRsvpResponse(guestId = it.guestId.toString(), rsvp = it.toResponse()) },
)

