package cloud.dywy.application.rsvp.result

import cloud.dywy.domain.rsvp.entity.GuestRsvp

sealed interface SubmitGuestRsvpResult {
    val rsvp: GuestRsvp

    data class Created(override val rsvp: GuestRsvp) : SubmitGuestRsvpResult
    data class Updated(override val rsvp: GuestRsvp) : SubmitGuestRsvpResult
}

