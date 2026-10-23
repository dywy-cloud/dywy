package cloud.dywy.application.rsvp

import cloud.dywy.application.rsvp.result.GetInvitationRsvpsResult
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import org.springframework.stereotype.Service

@Service
class InvitationRsvpGetter(
    private val guestRsvps: GuestRsvps,
) {

    fun get(invitationId: InvitationId): GetInvitationRsvpsResult =
        guestRsvps.findInvitationGuestRsvps(invitationId)
            .takeIf { it.isNotEmpty() }
            ?.let { GetInvitationRsvpsResult.Found(it) }
            ?: GetInvitationRsvpsResult.InvitationNotFound
}

