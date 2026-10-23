package cloud.dywy.application.rsvp

import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommand
import cloud.dywy.application.rsvp.result.SubmitInvitationRsvpsResult
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.invitation.repository.Invitations
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InvitationRsvpSubmitter(
    private val invitations: Invitations,
    private val guestRsvpSubmitter: GuestRsvpSubmitter,
    private val guestRsvps: GuestRsvps,
) {

    @Transactional
    fun submit(invitationId: InvitationId, commands: List<SubmitGuestRsvpCommand>): SubmitInvitationRsvpsResult =
        invitations.findById(invitationId)
            ?.let { invitation ->
                if (commands.all { command -> invitation.guests.any { it.id == command.guestId } }) {
                    commands.sortedBy { it.guestId.toString() }.forEach(guestRsvpSubmitter::submit)
                    SubmitInvitationRsvpsResult.Submitted(guestRsvps.findInvitationGuestRsvps(invitationId))
                } else {
                    SubmitInvitationRsvpsResult.GuestNotInInvitation
                }
            }
            ?: SubmitInvitationRsvpsResult.InvitationNotFound
}


