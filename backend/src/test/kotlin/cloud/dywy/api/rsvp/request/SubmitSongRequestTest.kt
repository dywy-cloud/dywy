package cloud.dywy.api.rsvp.request

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures
import kotlin.test.Test

class SubmitSongRequestTest {

    @Test
    fun `should map the request to a song choice`() {
        assertThat(SubmitSongRequestFixtures.laVieEnRose.toSongChoice()).isEqualTo(GuestRsvpFixtures.laVieEnRose)
    }
}

