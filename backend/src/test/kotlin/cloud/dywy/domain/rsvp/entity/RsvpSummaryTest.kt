package cloud.dywy.domain.rsvp.entity

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.guest.entity.GuestFixtures.johnDoe
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.janeDoeRsvpWithSameSyncedChoicesAsJohnDoe
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpDeclined
import kotlin.test.Test

class RsvpSummaryTest {

    private val janeDoeRsvpDeclined = janeDoeRsvpWithSameSyncedChoicesAsJohnDoe.respond(RsvpAttendance.DECLINED)


    @Test
    fun `should be pending when no guest answered`() {
        assertThat(RsvpSummary.of(listOf(johnDoe.id, janeDoe.id), emptyList()))
            .isEqualTo(RsvpSummary(RsvpStatus.PENDING, attending = 0, declined = 0, pending = 2, total = 2))
    }

    @Test
    fun `should be accepted all when every guest attends`() {
        val summary = RsvpSummary.of(
            listOf(johnDoe.id, janeDoe.id),
            listOf(johnDoeRsvp, janeDoeRsvpWithSameSyncedChoicesAsJohnDoe),
        )

        assertThat(summary)
            .isEqualTo(RsvpSummary(RsvpStatus.ACCEPTED_ALL, attending = 2, declined = 0, pending = 0, total = 2))
    }

    @Test
    fun `should be declined all when every guest declines`() {
        val summary = RsvpSummary.of(listOf(johnDoe.id, janeDoe.id), listOf(johnDoeRsvpDeclined, janeDoeRsvpDeclined))

        assertThat(summary)
            .isEqualTo(RsvpSummary(RsvpStatus.DECLINED_ALL, attending = 0, declined = 2, pending = 0, total = 2))
    }

    @Test
    fun `should be partially accepted when everyone answered with mixed answers`() {
        val summary = RsvpSummary.of(listOf(johnDoe.id, janeDoe.id), listOf(johnDoeRsvp, janeDoeRsvpDeclined))

        assertThat(summary)
            .isEqualTo(RsvpSummary(RsvpStatus.PARTIALLY_ACCEPTED, attending = 1, declined = 1, pending = 0, total = 2))
    }

    @Test
    fun `should be partially answered when a guest attends and another has no rsvp`() {
        val summary = RsvpSummary.of(listOf(johnDoe.id, janeDoe.id), listOf(johnDoeRsvp))

        assertThat(summary)
            .isEqualTo(RsvpSummary(RsvpStatus.PARTIALLY_ANSWERED, attending = 1, declined = 0, pending = 1, total = 2))
    }

    @Test
    fun `should be partially answered when a guest declines and another has no rsvp`() {
        val summary = RsvpSummary.of(listOf(johnDoe.id, janeDoe.id), listOf(johnDoeRsvpDeclined))

        assertThat(summary)
            .isEqualTo(RsvpSummary(RsvpStatus.PARTIALLY_ANSWERED, attending = 0, declined = 1, pending = 1, total = 2))
    }

    @Test
    fun `should be partially answered with mixed answers and a pending guest`() {
        val third = GuestId.generate()

        val summary = RsvpSummary.of(
            listOf(johnDoe.id, janeDoe.id, third),
            listOf(johnDoeRsvp, janeDoeRsvpDeclined),
        )

        assertThat(summary)
            .isEqualTo(RsvpSummary(RsvpStatus.PARTIALLY_ANSWERED, attending = 1, declined = 1, pending = 1, total = 3))
    }

    @Test
    fun `should be accepted all for a single attending guest`() {
        assertThat(RsvpSummary.of(listOf(johnDoe.id), listOf(johnDoeRsvp)).status).isEqualTo(RsvpStatus.ACCEPTED_ALL)
    }

    @Test
    fun `should ignore rsvps of guests outside the invitation`() {
        val summary = RsvpSummary.of(listOf(johnDoe.id), listOf(johnDoeRsvp, janeDoeRsvpDeclined))

        assertThat(summary)
            .isEqualTo(RsvpSummary(RsvpStatus.ACCEPTED_ALL, attending = 1, declined = 0, pending = 0, total = 1))
    }
}




