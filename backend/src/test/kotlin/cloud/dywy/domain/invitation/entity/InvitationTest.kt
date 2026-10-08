package cloud.dywy.domain.invitation.entity

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import kotlin.test.Test

class InvitationTest {

    @Test
    fun `should create invitation with guests`() {
        assertThat(bridesMaidInvitation.guests).isEqualTo(setOf(janeDoe))
    }

    @Test
    fun `should require a postal address when invitation is posted`() {
        assertFailure {
            brideFamilyInvitation.copy(deliveryMethod = DeliveryMethod.POSTED)
        }
            .isInstanceOf(IllegalArgumentException::class)
            .hasMessage("A posted invitation must include a postal address.")
    }

    @Test
    fun `should require at least one guest`() {
        assertFailure { Invitation(label = "Empty invitation", guests = emptySet(), description = "Empty invitation") }
            .isInstanceOf(IllegalArgumentException::class)
            .hasMessage("An invitation must include at least one guest.")
    }
}
