package cloud.dywy.api.invitation.response

import cloud.dywy.domain.rsvp.entity.RsvpAttendance

data class InvitationGuestResponse(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val attendance: RsvpAttendance = RsvpAttendance.PENDING,
)