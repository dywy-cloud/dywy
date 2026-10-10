package cloud.dywy.application.invitation.command

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import cloud.dywy.application.invitation.command.UpdateInvitationCommandFixtures.brideFamily
import cloud.dywy.domain.guest.entity.GuestFixtures.johnDoe
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import cloud.dywy.domain.invitation.entity.PostalAddressFixtures
import kotlin.test.Test

class UpdateInvitationCommandTest {

    private val postedInvitation = brideFamilyInvitation.copy(
        deliveryMethod = DeliveryMethod.POSTED,
        postalAddress = PostalAddressFixtures.paris,
    )

    @Test
    fun `should clear postal address when switching to hand delivered`() {
        val command = brideFamily.copy(deliveryMethod = DeliveryMethod.HAND_DELIVERED, postalAddress = null)

        val invitation = command.toInvitation(postedInvitation, setOf(johnDoe))

        assertThat(invitation.deliveryMethod).isEqualTo(DeliveryMethod.HAND_DELIVERED)
        assertThat(invitation.postalAddress).isNull()
    }

    @Test
    fun `should keep existing postal address when delivery method is not provided`() {
        val command = brideFamily.copy(deliveryMethod = null, postalAddress = null)

        val invitation = command.toInvitation(postedInvitation, setOf(johnDoe))

        assertThat(invitation.postalAddress).isEqualTo(PostalAddressFixtures.paris)
    }

    @Test
    fun `should replace postal address when posted with a new address`() {
        val command = brideFamily.copy(
            deliveryMethod = DeliveryMethod.POSTED,
            postalAddress = PostalAddressFixtures.geneva,
        )

        val invitation = command.toInvitation(postedInvitation, setOf(johnDoe))

        assertThat(invitation.postalAddress).isEqualTo(PostalAddressFixtures.geneva)
    }
}

