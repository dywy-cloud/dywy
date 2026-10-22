package cloud.dywy.application.rsvp

import cloud.dywy.application.rsvp.result.GetInvitationRsvpsResult
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.invitation.repository.Invitations
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import org.springframework.stereotype.Service

@Service
class InvitationRsvpGetter(
    private val invitations: Invitations,
    private val guestRsvps: GuestRsvps,
) {

    fun get(invitationId: InvitationId): GetInvitationRsvpsResult =
        invitations.findById(invitationId)
            ?.let { invitation ->
                GetInvitationRsvpsResult.Found(guestRsvps.findByGuestIds(invitation.guests.map { it.id }.toSet()))
            }
            ?: GetInvitationRsvpsResult.InvitationNotFound
}

