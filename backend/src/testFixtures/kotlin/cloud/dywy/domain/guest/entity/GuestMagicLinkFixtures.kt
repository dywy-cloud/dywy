package cloud.dywy.domain.guest.entity

import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import cloud.dywy.domain.shared.Dates

object GuestMagicLinkFixtures {
    val bridesMaidToJane = GuestMagicLink(
        invitationId = bridesMaidInvitation.id,
        guestId = janeDoe.id,
        token = GuestMagicLinkAccessToken(value = "53c2efcd-b4fc-42f3-a73b-fadf3725af3f"),
        expiresAt = Dates.nowUtc().plusMinutes(15)
    )
}
