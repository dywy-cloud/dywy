package cloud.dywy.application.guest

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.invitation.repository.Invitations
import org.springframework.stereotype.Service

@Service
class GuestSessionAuthorizer(
    private val invitations: Invitations,
) {

    fun isGuestInInvitation(invitationId: InvitationId, guestId: GuestId): Boolean =
        invitations.findById(invitationId)?.guests?.any { it.id == guestId } == true
}

