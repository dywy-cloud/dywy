package cloud.dywy.application.rsvp.result

import cloud.dywy.domain.rsvp.entity.GuestRsvp

sealed interface GetInvitationRsvpsResult {
    data class Found(val rsvps: List<GuestRsvp>) : GetInvitationRsvpsResult
    data object InvitationNotFound : GetInvitationRsvpsResult
}
