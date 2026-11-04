package cloud.dywy.api.invitation

import cloud.dywy.AbstractEndpointIntegrationTest
import cloud.dywy.api.guest.GuestApiTestHelper.createGuest
import cloud.dywy.api.guest.request.AddGuestRequest
import cloud.dywy.api.guest.request.AddGuestRequestFixtures.aliceSmith
import cloud.dywy.api.guest.request.AddGuestRequestFixtures.bobJohnson
import cloud.dywy.api.guest.request.AddGuestRequestFixtures.charlieDavis
import cloud.dywy.api.invitation.InvitationApiTestHelper.createInvitation
import cloud.dywy.api.invitation.request.AddInvitationRequest
import cloud.dywy.api.invitation.response.InvitationPageResponse
import cloud.dywy.api.invitation.response.InvitationResponse
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.rsvp.entity.GuestRsvp
import cloud.dywy.domain.rsvp.entity.RsvpAttendance
import cloud.dywy.domain.rsvp.entity.RsvpStatus
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.client.RestTestClient
import kotlin.test.Test

@Sql(scripts = ["classpath:data.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class InvitationRsvpStatusFilterIT : AbstractEndpointIntegrationTest() {

    @Autowired
    private lateinit var guestRsvps: GuestRsvps

    @ParameterizedTest
    @EnumSource(value = RsvpStatus::class, names = ["ACCEPTED_ALL", "DECLINED_ALL", "PARTIALLY_ACCEPTED"])
    fun `should list only the seeded invitations in the requested status`(status: RsvpStatus) {
        listInvitations("rsvpStatus=$status").assertOnlyStatus(status, count = 1)
    }

    @Test
    fun `should list an invitation without any reply as pending`() {
        val invitation = createInvitationOf(aliceSmith)

        listInvitations("rsvpStatus=PENDING")
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.totalItems").isEqualTo(1)
            .jsonPath("$.items[0].id").isEqualTo(invitation.id)
            .jsonPath("$.items[0].rsvpSummary.status").isEqualTo("PENDING")
    }

    @Test
    fun `should list an invitation with some replies missing as partially answered`() {
        val invitation = createInvitationOf(bobJohnson, charlieDavis)
        guestRsvps.save(GuestRsvp(guestId = GuestId.fromString(invitation.guests.first().id), attendance = RsvpAttendance.ATTENDING))

        listInvitations("rsvpStatus=PARTIALLY_ANSWERED")
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.totalItems").isEqualTo(1)
            .jsonPath("$.items[0].id").isEqualTo(invitation.id)
            .jsonPath("$.items[0].rsvpSummary.status").isEqualTo("PARTIALLY_ANSWERED")
    }

    @Test
    fun `should keep the filtered totals when requesting another page`() {
        listInvitations("rsvpStatus=ACCEPTED_ALL&page=1&size=1")
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(0)
            .jsonPath("$.totalItems").isEqualTo(1)
            .jsonPath("$.totalPages").isEqualTo(1)
    }

    @ParameterizedTest
    @CsvSource("ACCEPTED_ALL,1", "DECLINED_ALL,0")
    fun `should combine the rsvp status with the label`(status: RsvpStatus, count: Int) {
        listInvitations("label=${acceptedInvitationLabel()}&rsvpStatus=$status").assertOnlyStatus(status, count)
    }

    @ParameterizedTest
    @ValueSource(strings = ["rsvpStatus=MAYBE", "page=-1", "page=abc", "size=0"])
    fun `should return bad request when the query is invalid`(query: String) {
        listInvitations(query).expectStatus().isBadRequest
    }

    @Test
    fun `should accept an rsvp status in lower case`() {
        listInvitations("rsvpStatus=accepted_all").assertOnlyStatus(RsvpStatus.ACCEPTED_ALL, count = 1)
    }

    @Test
    fun `should ignore a blank rsvp status and label`() {
        listInvitations("rsvpStatus=&label= ")
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.totalItems").isEqualTo(totalItems())
    }

    @Test
    fun `should let a read-only user filter by rsvp status`() {
        listInvitations("rsvpStatus=ACCEPTED_ALL", email = READ_ONLY_EMAIL)
            .assertOnlyStatus(RsvpStatus.ACCEPTED_ALL, count = 1)
    }

    private fun listInvitations(query: String, email: String = ADMIN_EMAIL): RestTestClient.ResponseSpec =
        restTestClient.get().uri("/api/invitations?$query")
            .header(HttpHeaders.COOKIE, authenticatedCsrfContext(email).cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()

    private fun createInvitationOf(vararg guests: AddGuestRequest): InvitationResponse =
        authenticatedCsrfContext(ADMIN_EMAIL).let { csrf ->
            createInvitation(
                restTestClient, csrf, AddInvitationRequest(
                    label = "Filter ${guests.first().lastName}",
                    description = "Created by the RSVP status filter tests",
                    guestIds = guests.map { createGuest(restTestClient, csrf, it).id },
                )
            )
        }

    private fun acceptedInvitationLabel(): String =
        listInvitations("rsvpStatus=ACCEPTED_ALL")
            .expectBody(InvitationPageResponse::class.java)
            .returnResult()
            .responseBody
            ?.items
            ?.single()
            ?.label
            ?: error("Expected exactly one accepted invitation")

    private fun totalItems(query: String = "page=0"): Int =
        listInvitations(query)
            .expectBody(InvitationPageResponse::class.java)
            .returnResult()
            .responseBody
            ?.totalItems
            ?.toInt()
            ?: error("Missing invitation page")

    private fun RestTestClient.ResponseSpec.assertOnlyStatus(status: RsvpStatus, count: Int) {
        expectStatus().isOk
        expectBody()
            .jsonPath("$.items.length()").isEqualTo(count)
            .jsonPath("$.totalItems").isEqualTo(count)
            .jsonPath("$.items[?(@.rsvpSummary.status != '$status')]").isEmpty
    }

    private companion object {
        const val ADMIN_EMAIL = "gregory@example.com"
        const val READ_ONLY_EMAIL = "viewer@example.com"
    }
}
