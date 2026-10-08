package cloud.dywy.api.invitation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import cloud.dywy.application.invitation.command.UpdateInvitationCommand
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.PostalAddress

data class UpdateInvitationRequest(
    @field:PositiveOrZero
    val version: Long,
    @field:NotBlank
    val label: String,
    val description: String,
    val postalAddress: PostalAddress? = null,
    val deliveryMethod: DeliveryMethod? = null,
    val guestIds: List<String>,
) {

    internal fun toCommandOrNull(id: InvitationId): UpdateInvitationCommand? =
        guestIds
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { runCatching { GuestId.fromString(it) }.getOrNull() ?: return null }
            .toSet()
            .let {
                UpdateInvitationCommand(
                    id = id,
                    version = version,
                    label = label.trim(),
                    description = description.trim(),
                    guestIds = it,
                    postalAddress = postalAddress,
                    deliveryMethod = deliveryMethod
                )
            }
}