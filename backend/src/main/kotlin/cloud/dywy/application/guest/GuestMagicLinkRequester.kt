package cloud.dywy.application.guest

import io.github.oshai.kotlinlogging.KotlinLogging
import cloud.dywy.application.guest.command.RequestGuestMagicLinkCommand
import cloud.dywy.application.guest.result.RequestGuestMagicLinkResult
import cloud.dywy.application.invitation.InvitationTokenResolver
import cloud.dywy.domain.guest.entity.GuestMagicLink
import cloud.dywy.domain.guest.repository.GuestMagicLinkTokens
import cloud.dywy.domain.guest.service.GuestMagicLinkSender
import cloud.dywy.domain.shared.Dates.nowUtcMillis
import cloud.dywy.infrastructure.config.GuestAccessProperties
import cloud.dywy.infrastructure.shared.infoWithDetails
import cloud.dywy.infrastructure.shared.warnWithDetails
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class GuestMagicLinkRequester(
    private val invitationTokenResolver: InvitationTokenResolver,
    private val guestMagicLinkTokens: GuestMagicLinkTokens,
    private val guestMagicLinkSender: GuestMagicLinkSender,
    private val guestAccessProperties: GuestAccessProperties,
) {

    fun request(command: RequestGuestMagicLinkCommand): RequestGuestMagicLinkResult {
        val invitation = invitationTokenResolver.resolve(command.invitationAccessToken)
            ?: run {
                logger.warnWithDetails(message = "Magic-link request rejected: invitation not found") { "Magic-link request rejected: invitation not found (guestId=${command.guestId})" }
                return RequestGuestMagicLinkResult.InvitationNotFound
            }

        val guest = invitation.guests.firstOrNull { it.id == command.guestId }
            ?: run {
                logger.warnWithDetails(message = "Magic-link request rejected: guest not in invitation") { "Magic-link request rejected: guest not in invitation (invitationId=${invitation.id}, guestId=${command.guestId})" }
                return RequestGuestMagicLinkResult.GuestNotFound
            }

        return runCatching {
            val guestMagicLink = GuestMagicLink(
                invitationId = invitation.id,
                guestId = guest.id,
                expiresAt = nowUtcMillis().plusSeconds(guestAccessProperties.magicLinkTtlSeconds),
            ).also(guestMagicLinkTokens::create)

            guestMagicLinkSender.send(guestMagicLink, guest)
            logger.infoWithDetails("Magic-link request accepted") { "Magic-link request accepted (invitationId=${invitation.id}, guestId=${guest.id})" }
            RequestGuestMagicLinkResult.Sent
        }.getOrElse {
            logger.warnWithDetails(it, "Magic-link delivery failed") { "Magic-link delivery failed (invitationId=${invitation.id}, guestId=${guest.id})" }
            RequestGuestMagicLinkResult.DeliveryFailed
        }
    }

}
