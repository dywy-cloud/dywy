package cloud.dywy.api.invitation

import cloud.dywy.api.common.invitationAccessTokenPathParam
import cloud.dywy.api.invitation.response.toPublicResponse
import cloud.dywy.application.invitation.InvitationTokenResolver
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class GuestAccessInvitationEndpoint(
    private val invitationTokenResolver: InvitationTokenResolver,
) {

    fun resolveByAccessToken(request: ServerRequest): ServerResponse =
        request.invitationAccessTokenPathParam()
            ?.let { token ->
                invitationTokenResolver.resolve(token)
                    ?.toPublicResponse()
                    ?.let(ServerResponse.ok()::body)
                    ?: ServerResponse.notFound().build()
            }
            ?: ServerResponse.badRequest().build()
}




