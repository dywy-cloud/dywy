package cloud.dywy.infrastructure.invitation.repository

import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import kotlin.uuid.Uuid

object InvitationDataFixtures {

    const val unknownGuestId = "019fffff-0000-7000-8000-000000000001"
    const val malformedGuestId = "not-a-uuid"

    const val rawTokenWithMalformedGuestId = "6f1c1f5e-1a2b-4c3d-8e4f-000000000005"
    const val rawTokenWithInvalidPostalAddress = "6f1c1f5e-1a2b-4c3d-8e4f-000000000006"
    const val rawTokenWithUnknownProperty = "6f1c1f5e-1a2b-4c3d-8e4f-000000000001"
    const val rawTokenWithUnknownGuest = "6f1c1f5e-1a2b-4c3d-8e4f-000000000002"
    const val rawTokenPostedWithoutAddress = "6f1c1f5e-1a2b-4c3d-8e4f-000000000003"
    const val rawTokenWithoutGuests = "6f1c1f5e-1a2b-4c3d-8e4f-000000000004"

    val janeDoeId: String = janeDoe.id.value.toString()

    val guestsById: Map<Uuid, Guest> = mapOf(janeDoe.id.value to janeDoe)

    val bridesMaidData = InvitationData(
        label = bridesMaidInvitation.label,
        description = bridesMaidInvitation.description,
        deliveryMethod = bridesMaidInvitation.deliveryMethod,
        postalAddress = bridesMaidInvitation.postalAddress,
        guestIds = bridesMaidInvitation.guests.map { it.id.value.toString() },
        accessToken = bridesMaidInvitation.accessToken.value,
    )

    val postedWithoutPostalAddressData = bridesMaidData.copy(postalAddress = null)

    val malformedGuestIdData = bridesMaidData.copy(guestIds = listOf(malformedGuestId))

    fun InvitationData.rebuild(guests: Map<Uuid, Guest> = guestsById) = toInvitation(
        id = bridesMaidInvitation.id,
        version = bridesMaidInvitation.version,
        creationDate = bridesMaidInvitation.creationDate,
        updateDate = bridesMaidInvitation.updateDate,
        guestsById = guests,
    )

    fun rawJson(
        label: String = "Raw",
        description: String = "d",
        guestIds: List<String> = listOf(janeDoeId),
        accessToken: String? = rawTokenWithUnknownProperty,
        deliveryMethod: String? = null,
        extraProperties: String? = null,
    ): String = listOfNotNull(
        "\"label\":\"$label\"",
        "\"description\":\"$description\"",
        deliveryMethod?.let { "\"deliveryMethod\":\"$it\"" },
        "\"guestIds\":[${guestIds.joinToString(",") { "\"$it\"" }}]",
        accessToken?.let { "\"accessToken\":\"$it\"" },
        extraProperties,
    ).joinToString(",", "{", "}")

    val jsonWithUnknownProperty = rawJson(extraProperties = "\"futureField\":{\"a\":1}")
    val jsonWithoutOptionalProperties = rawJson()
    val jsonWithoutAccessToken = rawJson(accessToken = null)

    val jsonWithInvalidPostalAddress = rawJson(
        accessToken = rawTokenWithInvalidPostalAddress,
        extraProperties = "\"postalAddress\":{\"line1\":\" \",\"locality\":\"Paris\",\"countryCode\":\"ZZ\"}",
    )
}

