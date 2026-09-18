package cloud.dywy.application.rsvp

import cloud.dywy.application.rsvp.result.GetGuestRsvpResult
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import org.springframework.stereotype.Service

@Service
class GuestRsvpGetter(private val guestRsvps: GuestRsvps) {

    fun get(guestId: GuestId): GetGuestRsvpResult =
        guestRsvps.findByGuestId(guestId)
            ?.let(GetGuestRsvpResult::Submitted)
            ?: GetGuestRsvpResult.NotSubmittedYet
}

