package cloud.dywy.domain.rsvp.entity

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import cloud.dywy.domain.guest.entity.GuestFixtures.johnDoe
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeAnswers
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpDeclined
import cloud.dywy.domain.shared.Dates
import kotlin.test.Test

class GuestRsvpTest {

    @Test
    fun `should create a first response at version 1`() {
        val rsvp = GuestRsvp(guestId = johnDoe.id, attendance = RsvpAttendance.ATTENDING)

        assertThat(rsvp.version).isEqualTo(1L)
    }

    @Test
    fun `should bump version and update date when responding`() {
        val respondedRsvp = johnDoeRsvp.respond(
            attendance = RsvpAttendance.DECLINED,
            now = johnDoeRsvp.creationDate.plusDays(1),
        )

        assertThat(respondedRsvp).isEqualTo(johnDoeRsvpDeclined)
    }

    @Test
    fun `should default the update date to now when responding`() {
        val before = Dates.nowUtcMillis()

        val respondedRsvp = johnDoeRsvp.respond(RsvpAttendance.ATTENDING)

        assertThat(respondedRsvp.updateDate).isBetween(before, Dates.nowUtcMillis())
    }

    @Test
    fun `should keep the answers when responding as attending`() {
        val respondedRsvp = johnDoeRsvp.respond(RsvpAttendance.ATTENDING, johnDoeAnswers)

        assertThat(respondedRsvp.answers).isEqualTo(johnDoeAnswers)
    }

    @Test
    fun `should drop the answers when declining`() {
        val respondedRsvp = johnDoeRsvp.respond(RsvpAttendance.DECLINED, johnDoeAnswers)

        assertThat(respondedRsvp.answers).isNull()
    }

    @Test
    fun `should drop answers when created as declined`() {
        val rsvp = GuestRsvp(guestId = johnDoe.id, attendance = RsvpAttendance.DECLINED, answers = johnDoeAnswers)

        assertThat(rsvp.answers).isNull()
    }
}

