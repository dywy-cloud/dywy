package cloud.dywy.api.invitation.response

data class AlreadyAssignedInvitationGuestsResponse(
    val message: String,
    val guestIds: List<String>,
)