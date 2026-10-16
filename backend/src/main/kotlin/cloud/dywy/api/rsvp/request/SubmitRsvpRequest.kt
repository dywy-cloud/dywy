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
    internal fun toCommand(guestId: GuestId): SubmitGuestRsvpCommand? =
        when (val parsed = RsvpAttendance.parse(attendance)) {
            RsvpAttendance.PENDING -> null
            RsvpAttendance.DECLINED -> SubmitGuestRsvpCommand(guestId = guestId, attendance = parsed)
            RsvpAttendance.ATTENDING -> Meal.parseOrNull(meal)?.let { parsedMeal ->
                SubmitGuestRsvpCommand(
                    guestId = guestId,
                    attendance = parsed,
                    answers = RsvpAnswers(meal = parsedMeal, song = song?.toSongChoice()),
                )
            }
        }
}

