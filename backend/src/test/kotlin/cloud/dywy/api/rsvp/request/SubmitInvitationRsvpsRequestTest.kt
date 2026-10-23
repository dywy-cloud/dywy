package cloud.dywy.api.rsvp.request

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.sofiaAndLucas
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.sofiaTwice
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.sofiaWithInvalidAttendanceAndLucas
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.sofiaWithoutMealAndLucas
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.malformedGuestId
import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommandFixtures.sofiaMartinAttending
import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommandFixtures.lucasMartinDeclined
import kotlin.test.Test

class SubmitInvitationRsvpsRequestTest {

    @Test
    fun `should build one command per guest`() {
        assertThat(sofiaAndLucas.toCommands()).isNotNull().containsExactly(sofiaMartinAttending, lucasMartinDeclined)
    }

    @Test
    fun `should reject an entry with an invalid attendance`() {
        assertThat(sofiaWithInvalidAttendanceAndLucas.toCommands()).isNull()
    }

    @Test
    fun `should reject an attending entry without a meal`() {
        assertThat(sofiaWithoutMealAndLucas.toCommands()).isNull()
    }

    @Test
    fun `should reject a malformed guest id`() {
        assertThat(malformedGuestId.toCommands()).isNull()
    }

    @Test
    fun `should reject a guest submitted twice`() {
        assertThat(sofiaTwice.toCommands()).isNull()
    }
}



