package cloud.dywy.application.invitation

import cloud.dywy.domain.invitation.entity.InvitationListCriteria
import cloud.dywy.domain.invitation.repository.Invitations
import org.springframework.stereotype.Service

@Service
class InvitationLister(private val invitations: Invitations) {

    fun list(criteria: InvitationListCriteria) = invitations.list(criteria)
}
