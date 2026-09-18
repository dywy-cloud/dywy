package cloud.dywy.domain.guest.entity

import cloud.dywy.domain.invitation.entity.InvitationId

data class ConsumedGuestMagicLinkToken(
    val invitationId: InvitationId,
    val guestId: GuestId,
)