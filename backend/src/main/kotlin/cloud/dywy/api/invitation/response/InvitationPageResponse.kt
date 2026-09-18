package cloud.dywy.api.invitation.response

import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.InvitationPage

data class InvitationPageResponse(
    val items: List<InvitationResponse>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
)

internal fun InvitationPage.toResponse() = InvitationPageResponse(
    items = items.map(Invitation::toResponse),
    page = page,
    size = size,
    totalItems = totalItems,
    totalPages = totalPages,
)