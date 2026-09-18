package cloud.dywy.application.guest.result

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.Invitation

sealed interface GuestMagicLinkVerificationResult {

    data class Verified(
        val invitation: Invitation,
        val guestId: GuestId,
    ) : GuestMagicLinkVerificationResult

    data object InvalidOrExpiredOrUsedToken : GuestMagicLinkVerificationResult

    data object InvitationNotFound : GuestMagicLinkVerificationResult

    data object GuestNotInInvitation : GuestMagicLinkVerificationResult
}

