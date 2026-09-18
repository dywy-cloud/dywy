package cloud.dywy.api.invitation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import cloud.dywy.application.invitation.command.UpdateInvitationCommand
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.InvitationId

data class UpdateInvitationRequest(
    @field:PositiveOrZero
    val version: Long,
    @field:NotBlank
    val label: String,
    val description: String,
    val guestIds: List<String>,
) {

    internal fun toCommandOrNull(id: InvitationId): UpdateInvitationCommand? {
        val parsedGuestIds = guestIds
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { runCatching { GuestId.fromString(it) }.getOrNull() ?: return null }
            .toSet()

        return UpdateInvitationCommand(
            id = id,
            version = version,
            label = label.trim(),
            description = description.trim(),
            guestIds = parsedGuestIds,
        )
    }
}