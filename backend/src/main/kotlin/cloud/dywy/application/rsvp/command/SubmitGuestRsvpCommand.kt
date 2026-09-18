package cloud.dywy.application.rsvp.command

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.rsvp.entity.GuestRsvp
import cloud.dywy.domain.rsvp.entity.RsvpAnswers
import cloud.dywy.domain.rsvp.entity.RsvpAttendance

data class SubmitGuestRsvpCommand(
    val guestId: GuestId,
    val attendance: RsvpAttendance,
    val answers: RsvpAnswers? = null,
)

internal fun SubmitGuestRsvpCommand.toGuestRsvp() = GuestRsvp(
    guestId = guestId,
    attendance = attendance,
    answers = answers,
)

