package cloud.dywy.api.rsvp.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.api.rsvp.response.GuestRsvpResponseFixtures.johnDoe
import cloud.dywy.api.rsvp.response.GuestRsvpResponseFixtures.johnDoeMealOnly
import cloud.dywy.api.rsvp.response.GuestRsvpResponseFixtures.johnDoeWithChoices
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpMealOnly
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpWithChoices
import kotlin.test.Test

class GuestRsvpResponseTest {

    @Test
    fun `should map an rsvp without answers to a response`() {
        assertThat(johnDoeRsvp.toResponse()).isEqualTo(johnDoe)
    }

    @Test
    fun `should map an rsvp with meal and song to a response`() {
        assertThat(johnDoeRsvpWithChoices.toResponse()).isEqualTo(johnDoeWithChoices)
    }

    @Test
    fun `should map an rsvp with a meal but no song to a response`() {
        assertThat(johnDoeRsvpMealOnly.toResponse()).isEqualTo(johnDoeMealOnly)
    }
}

