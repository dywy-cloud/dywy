package cloud.dywy.domain.invitation.entity

import cloud.dywy.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.friendsInvitation

object InvitationPageFixtures {
    val firstPage = InvitationPage(
        items = listOf(brideFamilyInvitation, friendsInvitation),
        page = 0,
        size = 20,
        totalItems = 2,
        totalPages = 1,
    )
}

