package cloud.dywy.domain.rsvp.entity

import cloud.dywy.domain.guest.entity.GuestId

data class InvitationGuestRsvp(
    val guestId: GuestId,
    val firstName: String,
    val rsvp: GuestRsvp?,
)

