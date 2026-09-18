package cloud.dywy.api.invitation.request

import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.guest.entity.GuestFixtures.johnDoe

object AddInvitationRequestFixtures {
    val mixedGuestsWithWhitespace = AddInvitationRequest(
        label = "  Mixed guests  ",
        description = "  Mixed guests invitation  ",
        guestIds = listOf(" ${johnDoe.id} ", "", "   ", "${janeDoe.id}", "${johnDoe.id}"),
    )

    val blankLabel = AddInvitationRequest(
        label = "   ",
        description = "description",
        guestIds = listOf(johnDoe.id.toString()),
    )

    val malformedGuestId = AddInvitationRequest(
        label = "Bride Family",
        description = "description",
        guestIds = listOf(johnDoe.id.toString(), "not-a-uuid"),
    )

    val noGuest = AddInvitationRequest(
        label = "No guests",
        description = "No guests",
        guestIds = emptyList(),
    )
}

