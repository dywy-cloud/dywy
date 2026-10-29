package cloud.dywy.domain.invitation.entity

import cloud.dywy.domain.rsvp.entity.RsvpStatus

data class InvitationSearch(
    val criteria: InvitationListCriteria = InvitationListCriteria(),
    val rsvpStatus: RsvpStatus? = null,
)
