package cloud.dywy.application.invitation

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.rsvp.entity.GuestRsvp
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import org.springframework.stereotype.Service

@Service
class InvitationRsvpLoader(private val guestRsvps: GuestRsvps) {

    fun load(invitations: Collection<Invitation>): Map<GuestId, GuestRsvp> =
        guestRsvps.findByGuestIds(invitations.flatMap { it.guests }.map { it.id }.toSet()).associateBy { it.guestId }
}

