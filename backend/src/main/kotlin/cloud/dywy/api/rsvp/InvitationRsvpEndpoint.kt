package cloud.dywy.api.rsvp

import cloud.dywy.api.common.bindOrBadRequest
import cloud.dywy.api.common.requireGuestSession
import cloud.dywy.api.rsvp.request.SubmitInvitationRsvpsRequest
import cloud.dywy.api.rsvp.response.toInvitationResponse
import cloud.dywy.application.rsvp.InvitationRsvpGetter
import cloud.dywy.application.rsvp.InvitationRsvpSubmitter
import cloud.dywy.application.rsvp.result.GetInvitationRsvpsResult
import cloud.dywy.application.rsvp.result.SubmitInvitationRsvpsResult
import jakarta.validation.Validator
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class InvitationRsvpEndpoint(
    private val invitationRsvpSubmitter: InvitationRsvpSubmitter,
    private val invitationRsvpGetter: InvitationRsvpGetter,
    private val validator: Validator,
) {

    fun submit(request: ServerRequest): ServerResponse =
        request.bindOrBadRequest<SubmitInvitationRsvpsRequest>(validator) { payload ->
            payload.toCommands()
                ?.let { commands ->
                    when (val result = invitationRsvpSubmitter.submit(request.requireGuestSession().invitationId, commands)) {
                        is SubmitInvitationRsvpsResult.Submitted -> ServerResponse.ok().body(result.rsvps.toInvitationResponse())
                        SubmitInvitationRsvpsResult.InvitationNotFound,
                        SubmitInvitationRsvpsResult.GuestNotInInvitation,
                        -> ServerResponse.status(HttpStatus.FORBIDDEN).build()
                    }
                } ?: ServerResponse.badRequest().build()
        }

    fun fetch(request: ServerRequest): ServerResponse =
        when (val result = invitationRsvpGetter.get(request.requireGuestSession().invitationId)) {
            is GetInvitationRsvpsResult.Found -> ServerResponse.ok().body(result.rsvps.toInvitationResponse())
            GetInvitationRsvpsResult.InvitationNotFound -> ServerResponse.status(HttpStatus.FORBIDDEN).build()
        }
}

