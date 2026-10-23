package cloud.dywy.application.rsvp.result

import cloud.dywy.domain.rsvp.entity.InvitationGuestRsvp

sealed interface GetInvitationRsvpsResult {
    data class Found(val guests: List<InvitationGuestRsvp>) : GetInvitationRsvpsResult
    data object InvitationNotFound : GetInvitationRsvpsResult
}
