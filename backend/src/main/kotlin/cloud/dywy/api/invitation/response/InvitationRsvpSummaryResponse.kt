package cloud.dywy.api.invitation.response

import cloud.dywy.domain.rsvp.entity.RsvpStatus
import cloud.dywy.domain.rsvp.entity.RsvpSummary

data class InvitationRsvpSummaryResponse(
    val status: RsvpStatus,
    val attending: Int,
    val declined: Int,
    val pending: Int,
    val total: Int,
)

internal fun RsvpSummary.toResponse() = InvitationRsvpSummaryResponse(
    status = status,
    attending = attending,
    declined = declined,
    pending = pending,
    total = total,
)

