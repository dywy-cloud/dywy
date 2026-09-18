package cloud.dywy.domain.guest.entity

import cloud.dywy.domain.invitation.entity.InvitationId
import java.time.LocalDateTime

data class GuestMagicLink(
    val invitationId: InvitationId,
    val guestId: GuestId,
    val token: GuestMagicLinkAccessToken = GuestMagicLinkAccessToken(),
    val expiresAt: LocalDateTime
) {
    fun guestAccessPath() = "/api/guest-access/magic-links/${token.value}"
}
