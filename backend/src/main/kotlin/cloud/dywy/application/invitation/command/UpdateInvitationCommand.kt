package cloud.dywy.application.invitation.command

import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.PostalAddress
import cloud.dywy.domain.shared.Dates

data class UpdateInvitationCommand(
    val id: InvitationId,
    val version: Long,
    val label: String,
    val description: String,
    val guestIds: Set<GuestId>,
    val postalAddress: PostalAddress? = null,
    val deliveryMethod: DeliveryMethod? = null,
) {
    fun isMissingPostalAddress(existing: Invitation) =
        (deliveryMethod ?: existing.deliveryMethod) == DeliveryMethod.POSTED &&
            (postalAddress ?: existing.postalAddress) == null

    fun toInvitation(existing: Invitation, guests: Set<Guest>) =
        existing.copy(
            version = existing.version + 1,
            updateDate = Dates.nowUtcMillis(),
            label = label.trim(),
            description = description.trim(),
            postalAddress = postalAddress ?: existing.postalAddress,
            deliveryMethod = deliveryMethod ?: existing.deliveryMethod,
            guests = guests,
        )
}