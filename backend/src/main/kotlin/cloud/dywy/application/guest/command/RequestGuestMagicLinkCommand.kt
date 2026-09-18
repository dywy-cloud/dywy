package cloud.dywy.application.guest.command

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.InvitationAccessToken

data class RequestGuestMagicLinkCommand(
    val invitationAccessToken: InvitationAccessToken,
    val guestId: GuestId,
)