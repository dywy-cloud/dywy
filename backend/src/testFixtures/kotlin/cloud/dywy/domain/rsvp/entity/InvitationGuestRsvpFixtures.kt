package cloud.dywy.domain.rsvp.entity

import cloud.dywy.domain.guest.entity.GuestFixtures.lucasMartin
import cloud.dywy.domain.guest.entity.GuestFixtures.sofiaMartin
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.sofiaMartinRsvp

object InvitationGuestRsvpFixtures {
    val sofiaMartinAnswered = InvitationGuestRsvp(sofiaMartin.id, sofiaMartin.firstName, sofiaMartinRsvp)
    val lucasMartinPending = InvitationGuestRsvp(lucasMartin.id, lucasMartin.firstName, null)
}

