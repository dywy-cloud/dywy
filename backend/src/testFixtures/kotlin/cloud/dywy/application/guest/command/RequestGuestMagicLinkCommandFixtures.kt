package cloud.dywy.application.guest.command

import cloud.dywy.domain.guest.entity.GuestFixtures.albertEinstein
import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.invitation.entity.InvitationAccessToken
import cloud.dywy.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.unknownToken

object RequestGuestMagicLinkCommandFixtures {
    val validJaneDoe = RequestGuestMagicLinkCommand(
        invitationAccessToken = bridesMaidInvitation.accessToken,
        guestId = janeDoe.id,
    )

    val unknownInvitationForJaneDoe = RequestGuestMagicLinkCommand(
        invitationAccessToken = InvitationAccessToken.fromStringOrNull(unknownToken)
            ?: error("invalid fixture token"),
        guestId = janeDoe.id,
    )

    val guestNotInInvitation = RequestGuestMagicLinkCommand(
        invitationAccessToken = bridesMaidInvitation.accessToken,
        guestId = albertEinstein.id,
    )
}

