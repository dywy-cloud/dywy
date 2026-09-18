package cloud.dywy.application.invitation.command

import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.Invitation

data class AddInvitationCommand(
    val label: String,
    val description: String,
    val guestIds: Set<GuestId>,
) {
    fun toInvitation(guests: Set<Guest>) =
        Invitation(label = label.trim(), description = description.trim(), guests = guests)
}