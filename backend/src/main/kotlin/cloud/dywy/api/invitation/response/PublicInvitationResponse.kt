package cloud.dywy.api.invitation.response

import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.DeliveryMethod

data class PublicInvitationResponse(
    val label: String,
    val description: String,
    val deliveryMethod: DeliveryMethod? = null,
    val guests: List<PublicInvitationGuestResponse>,
    val guestCount: Int,
)

internal fun Invitation.toPublicResponse() = PublicInvitationResponse(
    label = label,
    description = description,
    deliveryMethod = deliveryMethod,
    guests = guests
        .sortedBy { it.id.toString() }
        .map {
            PublicInvitationGuestResponse(
                id = it.id.toString(),
                firstName = it.firstName,
                lastName = it.lastName,
            )
        },
    guestCount = guests.size,
)