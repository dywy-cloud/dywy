package cloud.dywy.api.invitation.request

import jakarta.validation.constraints.NotBlank
import cloud.dywy.application.invitation.command.AddInvitationCommand
import cloud.dywy.domain.guest.entity.GuestId

data class AddInvitationRequest(
    @field:NotBlank
    val label: String,
    val description: String,
    val guestIds: List<String>,
){
    internal fun toCommandOrNull(): AddInvitationCommand? {
        val parsedGuestIds = guestIds
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { runCatching { GuestId.fromString(it) }.getOrNull() ?: return null }
            .toSet()

        return AddInvitationCommand(
            label = label.trim(),
            description = description.trim(),
            guestIds = parsedGuestIds,
        )
    }
}