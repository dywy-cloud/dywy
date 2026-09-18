package cloud.dywy.application.guest

import io.github.oshai.kotlinlogging.KotlinLogging
import cloud.dywy.application.guest.result.GuestMagicLinkVerificationResult
import cloud.dywy.domain.guest.entity.GuestMagicLinkAccessToken
import cloud.dywy.domain.guest.repository.GuestMagicLinkTokens
import cloud.dywy.domain.invitation.repository.Invitations
import cloud.dywy.domain.shared.Dates.nowUtcMillis
import cloud.dywy.infrastructure.shared.infoWithDetails
import cloud.dywy.infrastructure.shared.warnWithDetails
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class GuestMagicLinkVerifier(
    private val guestMagicLinkTokens: GuestMagicLinkTokens,
    private val invitations: Invitations,
) {

    fun verify(token: GuestMagicLinkAccessToken): GuestMagicLinkVerificationResult {
        val consumedToken = guestMagicLinkTokens.consumeIfValid(token, nowUtcMillis())
            ?: run {
                logger.warn { "Magic-link verification failed: invalid, expired or already used token" }
                return GuestMagicLinkVerificationResult.InvalidOrExpiredOrUsedToken
            }
        val invitation = invitations.findById(consumedToken.invitationId)
            ?: run {
                logger.warnWithDetails(message = "Magic-link verification failed: invitation not found") { "Magic-link verification failed: invitation not found (invitationId=${consumedToken.invitationId})" }
                return GuestMagicLinkVerificationResult.InvitationNotFound
            }

        return invitation.guests
            .firstOrNull { it.id == consumedToken.guestId }
            ?.let {
                logger.infoWithDetails("Magic-link verified") { "Magic-link verified (invitationId=${invitation.id}, guestId=${it.id})" }
                GuestMagicLinkVerificationResult.Verified(invitation = invitation, guestId = it.id)
            }
            ?: run {
                logger.warnWithDetails(message = "Magic-link verification failed: guest not in invitation") { "Magic-link verification failed: guest not in invitation (invitationId=${invitation.id}, guestId=${consumedToken.guestId})" }
                GuestMagicLinkVerificationResult.GuestNotInInvitation
            }
    }
}

