package cloud.dywy.application.invitation.command

import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.shared.Dates

data class UpdateInvitationCommand(
    val id: InvitationId,
    val version: Long,
    val label: String,
    val description: String,
    val guestIds: Set<GuestId>,
) {
    fun toInvitation(existing: Invitation, guests: Set<Guest>) =
        existing.copy(
            version = existing.version + 1,
            updateDate = Dates.nowUtcMillis(),
            label = label.trim(),
            description = description.trim(),
            guests = guests,
        )
}