package cloud.dywy.api.rsvp.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.api.rsvp.response.InvitationRsvpsResponseFixtures.sofiaAnsweredLucasPending
import cloud.dywy.domain.rsvp.entity.InvitationGuestRsvpFixtures.lucasMartinPending
import cloud.dywy.domain.rsvp.entity.InvitationGuestRsvpFixtures.sofiaMartinAnswered
import kotlin.test.Test

class InvitationRsvpsResponseTest {

    @Test
    fun `should map every guest with the first name and the rsvp only when answered`() {
        assertThat(listOf(sofiaMartinAnswered, lucasMartinPending).toInvitationResponse()).isEqualTo(sofiaAnsweredLucasPending)
    }
}

