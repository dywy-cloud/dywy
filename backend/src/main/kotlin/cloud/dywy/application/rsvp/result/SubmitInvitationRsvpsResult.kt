package cloud.dywy.application.rsvp.result

import cloud.dywy.domain.rsvp.entity.InvitationGuestRsvp

sealed interface SubmitInvitationRsvpsResult {
    data class Submitted(val guests: List<InvitationGuestRsvp>) : SubmitInvitationRsvpsResult
    data object InvitationNotFound : SubmitInvitationRsvpsResult
    data object GuestNotInInvitation : SubmitInvitationRsvpsResult
}
