package cloud.dywy.application.rsvp.result

import cloud.dywy.domain.rsvp.entity.GuestRsvp

sealed interface SubmitInvitationRsvpsResult {
    data class Submitted(val rsvps: List<GuestRsvp>) : SubmitInvitationRsvpsResult
    data object InvitationNotFound : SubmitInvitationRsvpsResult
    data object GuestNotInInvitation : SubmitInvitationRsvpsResult
}
