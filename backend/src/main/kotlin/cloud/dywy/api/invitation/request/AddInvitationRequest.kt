package cloud.dywy.api.invitation.request

import jakarta.validation.constraints.NotBlank
import cloud.dywy.application.invitation.command.AddInvitationCommand
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.PostalAddress

data class AddInvitationRequest(
    @field:NotBlank
    val label: String,
    val description: String,
    val postalAddress: PostalAddress? = null,
    val deliveryMethod: DeliveryMethod? = null,
    val guestIds: List<String>,
) {
    internal fun toCommandOrNull(): AddInvitationCommand? =
        guestIds
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { runCatching { GuestId.fromString(it) }.getOrNull() ?: return null }
            .toSet()
            .let {
                AddInvitationCommand(
                    label = label.trim(),
                    description = description.trim(),
                    guestIds = it,
                    postalAddress = postalAddress,
                    deliveryMethod = deliveryMethod
                )
            }
}