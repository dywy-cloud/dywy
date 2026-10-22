package cloud.dywy.api.rsvp

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import cloud.dywy.AbstractEndpointIntegrationTest
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequest
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.sofiaAndForeignGuest
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.sofiaAndLucas
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.sofiaTwice
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.sofiaWithInvalidAttendanceAndLucas
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.malformedGuestId
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.noRsvps
import cloud.dywy.api.rsvp.response.InvitationRsvpsResponse
import cloud.dywy.domain.guest.entity.GuestFixtures.sofiaMartin
import cloud.dywy.domain.guest.entity.GuestFixtures.lucasMartin
import cloud.dywy.domain.invitation.entity.InvitationFixtures.martinFamilyInvitation
import cloud.dywy.support.FailingGuestRsvps
import cloud.dywy.support.FailingGuestRsvpsConfig
import cloud.dywy.support.RecordingWeddingPlaylist
import cloud.dywy.support.RecordingWeddingPlaylistConfig
import cloud.dywy.support.SynchronousAsyncConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.client.RestTestClient
import kotlin.test.BeforeTest
import kotlin.test.Test

@Import(RecordingWeddingPlaylistConfig::class, SynchronousAsyncConfig::class, FailingGuestRsvpsConfig::class)
@Sql(statements = ["DELETE FROM guest_rsvp"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class InvitationRsvpEndpointIT : AbstractEndpointIntegrationTest() {

    @Autowired
    private lateinit var recordingWeddingPlaylist: RecordingWeddingPlaylist

    @Autowired
    private lateinit var failingGuestRsvps: FailingGuestRsvps

    @BeforeTest
    fun resetPlaylist() {
        recordingWeddingPlaylist.reset()
        failingGuestRsvps.reset()
    }

    @Test
    fun `should reject invitation rsvps submission without guest session`() {
        val csrf = csrfContext()

        restTestClient.post().uri(RSVPS_URI)
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(sofiaAndLucas)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should reject invitation rsvps retrieval without guest session`() {
        restTestClient.get().uri(RSVPS_URI)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should save the rsvp of every guest of the invitation`() {
        submit(sofiaAndLucas).expectStatus().isOk

        assertThat(fetchSubmittedGuestIds()).containsExactlyInAnyOrder(sofiaMartin.id.toString(), lucasMartin.id.toString())
    }

    @Test
    fun `should return the saved rsvps in the submission response`() {
        val response = submit(sofiaAndLucas)
            .expectStatus().isOk
            .expectBody(InvitationRsvpsResponse::class.java)
            .returnResult()
            .responseBody

        assertThat(response?.rsvps?.map { it.guestId }.orEmpty())
            .containsExactlyInAnyOrder(sofiaMartin.id.toString(), lucasMartin.id.toString())
    }

    @Test
    fun `should return no rsvp when the invitation has none yet`() {
        assertThat(fetchSubmittedGuestIds()).isEmpty()
    }

    @Test
    fun `should reject a guest outside the invitation`() {
        submit(sofiaAndForeignGuest).expectStatus().isForbidden
    }

    @Test
    fun `should save nothing when a guest is outside the invitation`() {
        submit(sofiaAndForeignGuest)

        assertThat(fetchSubmittedGuestIds()).isEmpty()
    }

    @Test
    fun `should reject an invalid entry`() {
        submit(sofiaWithInvalidAttendanceAndLucas).expectStatus().isBadRequest
    }

    @Test
    fun `should save nothing when an entry is invalid`() {
        submit(sofiaWithInvalidAttendanceAndLucas)

        assertThat(fetchSubmittedGuestIds()).isEmpty()
    }

    @Test
    fun `should reject a malformed guest id`() {
        submit(malformedGuestId).expectStatus().isBadRequest
    }

    @Test
    fun `should reject a guest submitted twice`() {
        submit(sofiaTwice).expectStatus().isBadRequest
    }

    @Test
    fun `should reject an empty submission`() {
        submit(noRsvps).expectStatus().isBadRequest
    }

    @Test
    fun `should add the chosen song to the shared playlist after the commit`() {
        submit(sofiaAndLucas).expectStatus().isOk

        assertThat(recordingWeddingPlaylist.addedTrackIds).containsExactly(3135556L)
    }

    @Test
    fun `should not touch the shared playlist when the submission is rejected`() {
        submit(sofiaAndForeignGuest)

        assertThat(recordingWeddingPlaylist.addedTrackIds).isEmpty()
    }

    @Test
    fun `should save nothing when saving a guest fails`() {
        failingGuestRsvps.failSaving(lucasMartin.id)

        submit(sofiaAndLucas)

        assertThat(fetchSubmittedGuestIds()).isEmpty()
    }

    @Test
    fun `should not touch the shared playlist when saving a guest fails`() {
        failingGuestRsvps.failSaving(lucasMartin.id)

        submit(sofiaAndLucas)

        assertThat(recordingWeddingPlaylist.addedTrackIds).isEmpty()
    }

    private fun submit(request: SubmitInvitationRsvpsRequest): RestTestClient.ResponseSpec {
        val csrf = csrfContext()

        return restTestClient.post().uri(RSVPS_URI)
            .header(HttpHeaders.COOKIE, "${csrf.cookies}; ${friendsSessionCookie()}")
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
    }

    private fun fetchSubmittedGuestIds(): List<String> =
        restTestClient.get().uri(RSVPS_URI)
            .header(HttpHeaders.COOKIE, friendsSessionCookie())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(InvitationRsvpsResponse::class.java)
            .returnResult()
            .responseBody
            ?.rsvps
            ?.map { it.guestId }
            ?: error("Expected rsvps in response body")

    private fun friendsSessionCookie() = guestSessionCookie(sofiaMartin.id, martinFamilyInvitation.id)

    private companion object {
        const val RSVPS_URI = "/api/guest-access/secured/invitation/rsvps"
    }
}




