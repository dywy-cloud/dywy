package cloud.dywy.infrastructure.invitation.repository

import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.InvitationAccessToken
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.invitation.entity.PostalAddress
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import tools.jackson.core.JacksonException
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.time.LocalDateTime
import kotlin.uuid.Uuid

@JsonIgnoreProperties(ignoreUnknown = true)
data class InvitationData(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val label: String,
    val description: String,
    val deliveryMethod: DeliveryMethod? = null,
    val postalAddress: PostalAddress? = null,
    val guestIds: List<String>,
    val accessToken: String,
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

class InconsistentInvitationDataException(
    invitationId: InvitationId,
    reason: String,
    cause: Throwable? = null,
) : IllegalStateException("Inconsistent data for invitation ${invitationId.value}: $reason", cause)

internal val invitationDataMapper = jacksonObjectMapper()

internal fun InvitationData.toJson(): String = invitationDataMapper.writeValueAsString(this)

internal fun parseInvitationData(invitationId: InvitationId, json: String): InvitationData =
    try {
        invitationDataMapper.readValue<InvitationData>(json)
    } catch (e: JacksonException) {
        throw InconsistentInvitationDataException(
            invitationId,
            e.cause?.message ?: e.originalMessage ?: "unreadable invitation data",
            e,
        )
    }

internal fun Invitation.toData() = InvitationData(
    label = label,
    description = description,
    deliveryMethod = deliveryMethod,
    postalAddress = postalAddress,
    guestIds = guests.map { it.id.toString() }.sorted(),
    accessToken = accessToken.value,
)

internal fun InvitationData.toInvitation(
    id: InvitationId,
    version: Long,
    creationDate: LocalDateTime,
    updateDate: LocalDateTime,
    guestsById: Map<Uuid, Guest>,
): Invitation {
    val guests = parsedGuestIds(id).map { guestId ->
        guestsById[guestId]
            ?: throw InconsistentInvitationDataException(id, "missing guest $guestId")
    }.toSet()

    return try {
        Invitation(
            id = id,
            version = version,
            creationDate = creationDate,
            updateDate = updateDate,
            label = label,
            description = description,
            postalAddress = postalAddress,
            deliveryMethod = deliveryMethod,
            guests = guests,
            accessToken = InvitationAccessToken(accessToken),
        )
    } catch (e: IllegalArgumentException) {
        throw InconsistentInvitationDataException(id, e.message ?: "invalid invitation data", e)
    }
}

internal fun InvitationData.parsedGuestIds(invitationId: InvitationId): List<Uuid> =
    guestIds.map { guestId ->
        try {
            Uuid.parse(guestId)
        } catch (e: IllegalArgumentException) {
            throw InconsistentInvitationDataException(invitationId, "invalid guest id $guestId", e)
        }
    }

