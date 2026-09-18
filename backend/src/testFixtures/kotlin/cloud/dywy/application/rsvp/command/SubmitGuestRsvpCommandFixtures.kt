package cloud.dywy.application.rsvp.command

import cloud.dywy.domain.guest.entity.GuestFixtures.johnDoe
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.bohemianRhapsodyAnswers
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeAnswers
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.veggieAnswers
import cloud.dywy.domain.rsvp.entity.RsvpAttendance

object SubmitGuestRsvpCommandFixtures {

    val johnDoeAttending = SubmitGuestRsvpCommand(
        guestId = johnDoe.id,
        attendance = RsvpAttendance.ATTENDING,
        answers = johnDoeAnswers,
    )

    val johnDoeAttendingOtherSong = SubmitGuestRsvpCommand(
        guestId = johnDoe.id,
        attendance = RsvpAttendance.ATTENDING,
        answers = bohemianRhapsodyAnswers,
    )

    val johnDoeAttendingVeggie = SubmitGuestRsvpCommand(
        guestId = johnDoe.id,
        attendance = RsvpAttendance.ATTENDING,
        answers = veggieAnswers,
    )

    val johnDoeDeclined = SubmitGuestRsvpCommand(
        guestId = johnDoe.id,
        attendance = RsvpAttendance.DECLINED,
    )
}

