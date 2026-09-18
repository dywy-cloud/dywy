package cloud.dywy.domain.invitation.repository

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.InvitationAccessToken
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.invitation.entity.InvitationListCriteria
import cloud.dywy.domain.invitation.entity.InvitationPage

interface Invitations {

    fun add(invitation: Invitation): Invitation
    fun update(invitation: Invitation): Invitation?
    fun findById(id: InvitationId): Invitation?
    fun list(criteria: InvitationListCriteria): InvitationPage
    fun findAssignedGuestIds(guestIds: Set<GuestId>): Set<GuestId>
    fun findInvitationByAccessToken(token: InvitationAccessToken): Invitation?
}