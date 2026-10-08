package cloud.dywy.api.invitation.request

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isNotEmpty
import assertk.assertions.isNull
import jakarta.validation.Validation
import jakarta.validation.Validator
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.PostalAddressFixtures
import cloud.dywy.api.invitation.request.AddInvitationRequestFixtures.blankLabel
import cloud.dywy.api.invitation.request.AddInvitationRequestFixtures.malformedGuestId
import cloud.dywy.api.invitation.request.AddInvitationRequestFixtures.mixedGuestsWithWhitespace
import cloud.dywy.api.invitation.request.AddInvitationRequestFixtures.noGuest
import cloud.dywy.application.invitation.command.AddInvitationCommandFixtures.mixedGuests
import kotlin.test.BeforeTest
import cloud.dywy.application.invitation.command.AddInvitationCommandFixtures.noGuest as noGuestCommand
import kotlin.test.Test

class AddInvitationRequestTest {

    private lateinit var validator: Validator

    @BeforeTest
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `should have no validation errors for a valid request`() {
        val violations = validator.validate(mixedGuestsWithWhitespace)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should have a validation error when label is blank`() {
        val violations = validator.validate(blankLabel)

        assertThat(violations).isNotEmpty()
    }

    @Test
    fun `should map request to command`() {
        val command = mixedGuestsWithWhitespace.toCommandOrNull()

        assertThat(command).isEqualTo(mixedGuests)
    }

    @Test
    fun `should return null when a guest id is malformed`() {
        val command = malformedGuestId.toCommandOrNull()

        assertThat(command).isNull()
    }

    @Test
    fun `should map request with no guest ids to empty set`() {
        val command = noGuest.toCommandOrNull()

        assertThat(command).isEqualTo(noGuestCommand)
    }

    @Test
    fun `should keep delivery method when mapping request to command`() {
        val request = AddInvitationRequest(
            label = "Mixed guests",
            description = "Mixed guests invitation",
            deliveryMethod = DeliveryMethod.POSTED,
            guestIds = listOf(mixedGuestsWithWhitespace.guestIds.first())
        )

        val command = request.toCommandOrNull()

        assertThat(command?.deliveryMethod).isEqualTo(DeliveryMethod.POSTED)
    }

    @Test
    fun `should keep postal address when mapping request to command`() {
        val request = AddInvitationRequest(
            label = "Mixed guests",
            description = "Mixed guests invitation",
            postalAddress = PostalAddressFixtures.paris,
            deliveryMethod = DeliveryMethod.POSTED,
            guestIds = listOf(mixedGuestsWithWhitespace.guestIds.first())
        )

        assertThat(request.toCommandOrNull()?.postalAddress).isEqualTo(PostalAddressFixtures.paris)
    }

    @Test
    fun `should map posted request without postal address and leave validation to application`() {
        val request = AddInvitationRequest(
            label = "Mixed guests",
            description = "Mixed guests invitation",
            deliveryMethod = DeliveryMethod.POSTED,
            guestIds = listOf(mixedGuestsWithWhitespace.guestIds.first())
        )

        val command = request.toCommandOrNull()

        assertThat(command?.postalAddress).isNull()
        assertThat(command?.isMissingPostalAddress()).isEqualTo(true)
    }
}
