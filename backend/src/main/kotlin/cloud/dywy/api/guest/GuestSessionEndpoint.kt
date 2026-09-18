package cloud.dywy.api.guest

import cloud.dywy.api.common.requireGuestSession
import cloud.dywy.api.invitation.response.GuestSessionResponse
import cloud.dywy.application.guest.GuestSessionResolver
import cloud.dywy.application.guest.result.GuestSessionResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class GuestSessionEndpoint(
    private val guestSessionResolver: GuestSessionResolver,
) {

    fun me(request: ServerRequest): ServerResponse {
        val guestSession = request.requireGuestSession()

        return when (val result = guestSessionResolver.resolve(guestSession)) {
            is GuestSessionResult.Resolved -> ServerResponse.ok().body(
                GuestSessionResponse(
                    guestId = "${guestSession.guestId}",
                    invitationId = "${guestSession.invitationId}",
                    firstName = result.guest.firstName,
                    lastName = result.guest.lastName,
                    language = result.guest.language.name,
                )
            )

            GuestSessionResult.InvitationNotFound,
            GuestSessionResult.GuestNotInInvitation,
            -> ServerResponse.status(HttpStatus.FORBIDDEN).build()
        }
    }
}




