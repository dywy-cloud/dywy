package cloud.dywy.api.rsvp.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.api.rsvp.response.InvitationRsvpsResponseFixtures.sofiaAndLucas
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.lucasMartinRsvpDeclined
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.sofiaMartinRsvp
import kotlin.test.Test

class InvitationRsvpsResponseTest {

    @Test
    fun `should map the rsvps to a response keyed by guest id`() {
        assertThat(listOf(sofiaMartinRsvp, lucasMartinRsvpDeclined).toInvitationResponse()).isEqualTo(sofiaAndLucas)
    }
}

