package cloud.dywy.api.rsvp.request

import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommand
import cloud.dywy.domain.guest.entity.GuestId
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class SubmitInvitationRsvpsRequest(
    @field:NotEmpty
    @field:Size(max = 50)
    @field:Valid
    val rsvps: List<InvitationGuestRsvpRequest>,
) {
    internal fun toCommands(): List<SubmitGuestRsvpCommand>? =
        rsvps.map { it.toCommand() ?: return null }
            .takeIf { commands -> commands.map { it.guestId }.toSet().size == commands.size }
}

data class InvitationGuestRsvpRequest(
    val guestId: String,
    @field:Valid
    val rsvp: SubmitRsvpRequest,
) {
    internal fun toCommand(): SubmitGuestRsvpCommand? =
        GuestId.fromStringOrNull(guestId)?.let(rsvp::toCommand)
}

