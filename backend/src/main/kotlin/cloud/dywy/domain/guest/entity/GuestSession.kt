package cloud.dywy.domain.guest.entity

import cloud.dywy.domain.invitation.entity.InvitationId

data class GuestSession(
    val guestId: GuestId,
    val invitationId: InvitationId,
)

