package cloud.dywy.api.invitation.response

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.InvitationPage
import cloud.dywy.domain.rsvp.entity.GuestRsvp

data class InvitationPageResponse(
    val items: List<InvitationResponse>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
)

internal fun InvitationPage.toResponse(rsvps: Map<GuestId, GuestRsvp>) = InvitationPageResponse(
    items = items.map { it.toResponse(rsvps) },
    page = page,
    size = size,
    totalItems = totalItems,
    totalPages = totalPages,
)