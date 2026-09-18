package cloud.dywy.application.guest

import cloud.dywy.application.guest.result.GuestSessionResult
import cloud.dywy.domain.guest.entity.GuestSession
import cloud.dywy.domain.invitation.repository.Invitations
import org.springframework.stereotype.Service

@Service
class GuestSessionResolver(private val invitations: Invitations) {

    fun resolve(session: GuestSession): GuestSessionResult {
        val invitation = invitations.findById(session.invitationId)
            ?: return GuestSessionResult.InvitationNotFound

        return invitation.guests
            .firstOrNull { it.id == session.guestId }
            ?.let { GuestSessionResult.Resolved(it) }
            ?: GuestSessionResult.GuestNotInInvitation
    }
}

