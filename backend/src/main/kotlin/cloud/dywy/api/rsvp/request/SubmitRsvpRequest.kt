package cloud.dywy.api.rsvp.request

import jakarta.validation.constraints.NotBlank
import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommand
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.rsvp.entity.Meal
import cloud.dywy.domain.rsvp.entity.RsvpAnswers
import cloud.dywy.domain.rsvp.entity.RsvpAttendance

data class SubmitRsvpRequest(
    @field:NotBlank
    val attendance: String,
    val meal: String? = null,
    val song: SubmitSongRequest? = null,
) {
    /**
     * Builds the command, or returns `null` (→ 400) for an invalid payload:
     * an unknown attendance, or — when attending — a missing/unknown meal.
     * Choices are ignored for a declined guest.
     */
    internal fun toCommand(guestId: GuestId): SubmitGuestRsvpCommand? {
        val attendance = RsvpAttendance.parseOrNull(attendance) ?: return null

        if (attendance != RsvpAttendance.ATTENDING) {
            return SubmitGuestRsvpCommand(guestId = guestId, attendance = attendance)
        }

        val meal = Meal.parseOrNull(meal) ?: return null
        return SubmitGuestRsvpCommand(
            guestId = guestId,
            attendance = attendance,
            answers = RsvpAnswers(meal = meal, song = song?.toSongChoice()),
        )
    }
}

