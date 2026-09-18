package cloud.dywy.api.rsvp.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.api.rsvp.response.SongResponseFixtures.laVieEnRose
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures
import kotlin.test.Test

class SongResponseTest {

    @Test
    fun `should map a song choice to a response`() {
        assertThat(GuestRsvpFixtures.laVieEnRose.toResponse()).isEqualTo(laVieEnRose)
    }
}

