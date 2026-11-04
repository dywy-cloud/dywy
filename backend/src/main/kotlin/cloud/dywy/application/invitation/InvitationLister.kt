package cloud.dywy.application.invitation

import cloud.dywy.domain.invitation.entity.InvitationPage
import cloud.dywy.domain.invitation.entity.InvitationSearch
import cloud.dywy.domain.invitation.repository.Invitations
import org.springframework.stereotype.Service

@Service
class InvitationLister(
    private val invitations: Invitations,
) {

    fun list(search: InvitationSearch): InvitationPage =
        search.rsvpStatus?.let { invitations.listByRsvpStatus(search.criteria, it) } ?: invitations.list(search.criteria)
}
