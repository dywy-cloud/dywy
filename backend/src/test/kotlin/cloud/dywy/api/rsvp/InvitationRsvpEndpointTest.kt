package cloud.dywy.api.rsvp

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequest
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.sofiaAndLucas
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.sofiaWithInvalidAttendanceAndLucas
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequestFixtures.tooManyRsvps
import cloud.dywy.application.rsvp.InvitationRsvpGetter
import cloud.dywy.application.rsvp.InvitationRsvpSubmitter
import cloud.dywy.application.rsvp.result.GetInvitationRsvpsResult
import cloud.dywy.application.rsvp.result.SubmitInvitationRsvpsResult
import cloud.dywy.domain.guest.entity.GuestFixtures.sofiaMartin
import cloud.dywy.domain.guest.entity.GuestSession
import cloud.dywy.domain.invitation.entity.InvitationFixtures.martinFamilyInvitation
import cloud.dywy.domain.rsvp.entity.InvitationGuestRsvpFixtures.lucasMartinPending
import cloud.dywy.domain.rsvp.entity.InvitationGuestRsvpFixtures.sofiaMartinAnswered
import cloud.dywy.infrastructure.guest.security.GuestSessionAuthenticationToken
import io.mockk.every
import io.mockk.mockk
import jakarta.validation.Validation
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.function.ServerRequest
import java.util.Optional
import kotlin.test.BeforeTest
import kotlin.test.Test

class InvitationRsvpEndpointTest {

    private val session = GuestSession(guestId = sofiaMartin.id, invitationId = martinFamilyInvitation.id)

    private lateinit var invitationRsvpSubmitter: InvitationRsvpSubmitter
    private lateinit var invitationRsvpGetter: InvitationRsvpGetter
    private lateinit var invitationRsvpEndpoint: InvitationRsvpEndpoint

    @BeforeTest
    fun setUp() {
        invitationRsvpSubmitter = mockk()
        invitationRsvpGetter = mockk()
        invitationRsvpEndpoint = InvitationRsvpEndpoint(
            invitationRsvpSubmitter,
            invitationRsvpGetter,
            Validation.buildDefaultValidatorFactory().validator,
        )
    }

    @Test
    fun `should return ok when every rsvp is saved`() {
        every { invitationRsvpSubmitter.submit(martinFamilyInvitation.id, any()) } returns
            SubmitInvitationRsvpsResult.Submitted(listOf(sofiaMartinAnswered, lucasMartinPending))

        assertThat(invitationRsvpEndpoint.submit(submitRequest(sofiaAndLucas)).statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `should reject a submission containing a guest outside the invitation`() {
        every { invitationRsvpSubmitter.submit(martinFamilyInvitation.id, any()) } returns
            SubmitInvitationRsvpsResult.GuestNotInInvitation

        assertThat(invitationRsvpEndpoint.submit(submitRequest(sofiaAndLucas)).statusCode()).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `should reject a submission for an unknown invitation`() {
        every { invitationRsvpSubmitter.submit(martinFamilyInvitation.id, any()) } returns
            SubmitInvitationRsvpsResult.InvitationNotFound

        assertThat(invitationRsvpEndpoint.submit(submitRequest(sofiaAndLucas)).statusCode()).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `should reject a submission with an invalid entry`() {
        assertThat(invitationRsvpEndpoint.submit(submitRequest(sofiaWithInvalidAttendanceAndLucas)).statusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should reject a submission with too many entries`() {
        assertThat(invitationRsvpEndpoint.submit(submitRequest(tooManyRsvps)).statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return the rsvps of the session invitation`() {
        every { invitationRsvpGetter.get(martinFamilyInvitation.id) } returns
            GetInvitationRsvpsResult.Found(listOf(sofiaMartinAnswered))

        assertThat(invitationRsvpEndpoint.fetch(authenticatedRequest()).statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `should reject the retrieval for an unknown invitation`() {
        every { invitationRsvpGetter.get(martinFamilyInvitation.id) } returns GetInvitationRsvpsResult.InvitationNotFound

        assertThat(invitationRsvpEndpoint.fetch(authenticatedRequest()).statusCode()).isEqualTo(HttpStatus.FORBIDDEN)
    }

    private fun submitRequest(payload: SubmitInvitationRsvpsRequest): ServerRequest = mockk {
        every { principal() } returns Optional.of(GuestSessionAuthenticationToken.authenticated(session))
        every { body(SubmitInvitationRsvpsRequest::class.java) } returns payload
    }

    private fun authenticatedRequest(): ServerRequest = mockk {
        every { principal() } returns Optional.of(GuestSessionAuthenticationToken.authenticated(session))
    }
}
