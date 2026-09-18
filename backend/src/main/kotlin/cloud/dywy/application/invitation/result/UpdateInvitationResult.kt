package cloud.dywy.application.invitation.result

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.Invitation

sealed interface UpdateInvitationResult {
    data class Updated(val invitation: Invitation) : UpdateInvitationResult
    data object NotFound : UpdateInvitationResult
    data object VersionConflict : UpdateInvitationResult
    data object MissingGuests : UpdateInvitationResult
    data class InvalidGuests(val guestIds: Set<GuestId>) : UpdateInvitationResult
    data class AlreadyAssignedGuests(val guestIds: Set<GuestId>) : UpdateInvitationResult
}
