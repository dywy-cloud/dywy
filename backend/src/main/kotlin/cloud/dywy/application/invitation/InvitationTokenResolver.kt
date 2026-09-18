package cloud.dywy.application.invitation

import cloud.dywy.domain.invitation.entity.InvitationAccessToken
import cloud.dywy.domain.invitation.repository.Invitations
import org.springframework.stereotype.Service

@Service
class InvitationTokenResolver(private val invitations: Invitations) {

    fun resolve(token: InvitationAccessToken) = invitations.findInvitationByAccessToken(token)
}

