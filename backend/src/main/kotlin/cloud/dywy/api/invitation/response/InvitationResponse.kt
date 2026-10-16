package cloud.dywy.api.invitation.response

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.PostalAddress
import cloud.dywy.domain.rsvp.entity.GuestRsvp
import cloud.dywy.domain.rsvp.entity.RsvpAttendance
import cloud.dywy.domain.rsvp.entity.RsvpSummary

data class InvitationResponse(
    val id: String,
    val accessToken: String,
    val version: Long,
    val creationDate: String,
    val updateDate: String,
    val label: String,
    val description: String,
    val postalAddress: PostalAddress? = null,
    val deliveryMethod: DeliveryMethod? = null,
    val guests: List<InvitationGuestResponse>,
    val guestCount: Int,
    val rsvpSummary: InvitationRsvpSummaryResponse,
)

internal fun Invitation.toResponse(rsvps: Map<GuestId, GuestRsvp>) =
    InvitationResponse(
        id = id.toString(),
        accessToken = accessToken.value,
        version = version,
        creationDate = creationDate.toString(),
        updateDate = updateDate.toString(),
        label = label,
        description = description,
        postalAddress = postalAddress,
        deliveryMethod = deliveryMethod,
        guests = guests
            .sortedBy { it.id.toString() }
            .map {
                InvitationGuestResponse(
                    id = it.id.toString(),
                    firstName = it.firstName,
                    lastName = it.lastName,
                    email = it.email,
                    attendance = rsvps[it.id]?.attendance ?: RsvpAttendance.PENDING,
                )
            },
        guestCount = guests.size,
        rsvpSummary = RsvpSummary.of(guests.map { it.id }, guests.mapNotNull { rsvps[it.id] }).toResponse(),
    )
