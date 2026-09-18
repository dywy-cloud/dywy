package cloud.dywy.api.invitation.response

data class InvalidInvitationGuestsResponse(
    val message: String,
    val guestIds: List<String>,
)