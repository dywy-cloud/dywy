package cloud.dywy.application.invitation.command

import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.PostalAddress

data class AddInvitationCommand(
    val label: String,
    val description: String,
    val guestIds: Set<GuestId>,
    val postalAddress: PostalAddress? = null,
    val deliveryMethod: DeliveryMethod? = null,
) {
    fun isMissingPostalAddress() = deliveryMethod == DeliveryMethod.POSTED && postalAddress == null

    fun toInvitation(guests: Set<Guest>) =
        Invitation(
            label = label.trim(),
            description = description.trim(),
            postalAddress = postalAddress,
            deliveryMethod = deliveryMethod,
            guests = guests,
        )
}