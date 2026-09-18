package cloud.dywy.application.rsvp.result

import cloud.dywy.domain.rsvp.entity.GuestRsvp

sealed interface GetGuestRsvpResult {
    data class Submitted(val rsvp: GuestRsvp) : GetGuestRsvpResult
    data object NotSubmittedYet : GetGuestRsvpResult
}

