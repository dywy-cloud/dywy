package cloud.dywy.application.invitation.result

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.Invitation

sealed interface AddInvitationResult {
    data class Added(val invitation: Invitation) : AddInvitationResult
    data object MissingGuests : AddInvitationResult
    data class InvalidGuests(val guestIds: Set<GuestId>) : AddInvitationResult
    data class AlreadyAssignedGuests(val guestIds: Set<GuestId>) : AddInvitationResult
}

