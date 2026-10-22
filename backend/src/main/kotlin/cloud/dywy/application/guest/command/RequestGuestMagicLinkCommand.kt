package cloud.dywy.application.guest.command

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.guest.entity.GuestMagicLinkMode
import cloud.dywy.domain.invitation.entity.InvitationAccessToken

data class RequestGuestMagicLinkCommand(
    val invitationAccessToken: InvitationAccessToken,
    val guestId: GuestId,
    val mode: GuestMagicLinkMode = GuestMagicLinkMode.INDIVIDUAL,
)