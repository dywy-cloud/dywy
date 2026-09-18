package cloud.dywy.application.invitation

import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.invitation.repository.Invitations
import org.springframework.stereotype.Service

@Service
class InvitationGetter(private val invitations: Invitations) {

    fun get(id: InvitationId) = invitations.findById(id)
}
