package cloud.dywy.domain.rsvp.entity

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.shared.Dates
import java.time.LocalDateTime

data class GuestRsvp(
    val id: GuestRsvpId = GuestRsvpId(),
    val guestId: GuestId,
    val version: Long = 1L,
    val creationDate: LocalDateTime = Dates.nowUtcMillis(),
    val updateDate: LocalDateTime = Dates.nowUtcMillis(),
    val attendance: RsvpAttendance,
    var answers: RsvpAnswers? = null,
) {
    init {
        if (attendance != RsvpAttendance.ATTENDING) {
            answers = null
        }
    }

    fun respond(
        attendance: RsvpAttendance,
        answers: RsvpAnswers? = null,
        now: LocalDateTime = Dates.nowUtcMillis(),
    ) = copy(
        version = version + 1,
        updateDate = now,
        attendance = attendance,
        answers = answers,
    )
}

