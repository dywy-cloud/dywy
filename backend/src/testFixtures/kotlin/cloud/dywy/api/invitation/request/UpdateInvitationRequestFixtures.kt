package cloud.dywy.api.invitation.request

import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.guest.entity.GuestFixtures.johnDoe
import cloud.dywy.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation

object UpdateInvitationRequestFixtures {
    val mixedGuestsWithWhitespace = UpdateInvitationRequest(
        version = brideFamilyInvitation.version,
        label = "  Mixed guests  ",
        description = "  Mixed guests invitation  ",
        guestIds = listOf(" ${johnDoe.id} ", "", "   ", "${janeDoe.id}", "${johnDoe.id}"),
    )

    val blankLabel = UpdateInvitationRequest(
        version = brideFamilyInvitation.version,
        label = "   ",
        description = "description",
        guestIds = listOf(johnDoe.id.toString()),
    )

    val malformedGuestId = UpdateInvitationRequest(
        version = brideFamilyInvitation.version,
        label = "Bride Family",
        description = "description",
        guestIds = listOf(johnDoe.id.toString(), "not-a-uuid"),
    )

    val noGuest = UpdateInvitationRequest(
        version = brideFamilyInvitation.version,
        label = "No guests",
        description = "No guests",
        guestIds = emptyList(),
    )
}

