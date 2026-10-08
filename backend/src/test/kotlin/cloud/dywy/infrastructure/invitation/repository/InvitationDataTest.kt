package cloud.dywy.infrastructure.invitation.repository

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.messageContains
import assertk.assertions.prop
import cloud.dywy.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.bridesMaidData
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.janeDoeId
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.jsonWithUnknownProperty
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.jsonWithInvalidPostalAddress
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.jsonWithoutAccessToken
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.jsonWithoutOptionalProperties
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.malformedGuestId
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.malformedGuestIdData
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.postedWithoutPostalAddressData
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.rebuild
import tools.jackson.core.JacksonException
import tools.jackson.module.kotlin.readValue
import kotlin.test.Test

class InvitationDataTest {

    @Test
    fun `should map invitation to its data`() {
        assertThat(bridesMaidInvitation.toData()).isEqualTo(bridesMaidData)
    }

    @Test
    fun `should round trip invitation data through json`() {
        val json = invitationDataMapper.writeValueAsString(bridesMaidData)

        assertThat(invitationDataMapper.readValue<InvitationData>(json)).isEqualTo(bridesMaidData)
    }

    @Test
    fun `should rebuild the invitation from its data`() {
        assertThat(bridesMaidData.rebuild()).isEqualTo(bridesMaidInvitation)
    }

    @Test
    fun `should ignore unknown properties`() {
        val data = invitationDataMapper.readValue<InvitationData>(jsonWithUnknownProperty)

        assertThat(data.guestIds).isEqualTo(listOf(janeDoeId))
    }

    @Test
    fun `should default schema version, delivery method and postal address when absent`() {
        val data = invitationDataMapper.readValue<InvitationData>(jsonWithoutOptionalProperties)

        assertThat(data.schemaVersion).isEqualTo(InvitationData.CURRENT_SCHEMA_VERSION)
        assertThat(data.deliveryMethod).isNull()
        assertThat(data.postalAddress).isNull()
    }

    @Test
    fun `should fail when a required property is missing`() {
        assertFailure { invitationDataMapper.readValue<InvitationData>(jsonWithoutAccessToken) }
            .isInstanceOf(JacksonException::class)
    }

    @Test
    fun `should parse stored json`() {
        val json = bridesMaidData.toJson()

        assertThat(parseInvitationData(bridesMaidInvitation.id, json)).isEqualTo(bridesMaidData)
    }

    @Test
    fun `should report an invalid nested postal address with the invitation id`() {
        assertFailure { parseInvitationData(bridesMaidInvitation.id, jsonWithInvalidPostalAddress) }
            .isInstanceOf(InconsistentInvitationDataException::class)
            .all {
                messageContains("Inconsistent data for invitation ${bridesMaidInvitation.id.value}")
                messageContains("Invalid postal address")
            }
    }

    @Test
    fun `should report a missing required property with the invitation id`() {
        assertFailure { parseInvitationData(bridesMaidInvitation.id, jsonWithoutAccessToken) }
            .isInstanceOf(InconsistentInvitationDataException::class)
            .messageContains("Inconsistent data for invitation ${bridesMaidInvitation.id.value}")
    }

    @Test
    fun `should report malformed json with the invitation id`() {
        assertFailure { parseInvitationData(bridesMaidInvitation.id, "{not json") }
            .isInstanceOf(InconsistentInvitationDataException::class)
            .messageContains("Inconsistent data for invitation ${bridesMaidInvitation.id.value}")
    }

    @Test
    fun `should fail with a dedicated exception when a guest is missing`() {
        assertFailure { bridesMaidData.rebuild(guests = emptyMap()) }
            .isInstanceOf(InconsistentInvitationDataException::class)
            .hasMessage("Inconsistent data for invitation ${bridesMaidInvitation.id.value}: missing guest $janeDoeId")
    }

    @Test
    fun `should fail with a dedicated exception when a guest id is malformed`() {
        assertFailure { malformedGuestIdData.rebuild() }
            .isInstanceOf(InconsistentInvitationDataException::class)
            .hasMessage("Inconsistent data for invitation ${bridesMaidInvitation.id.value}: invalid guest id $malformedGuestId")
    }

    @Test
    fun `should fail with a dedicated exception when stored data breaks domain rules`() {
        assertFailure { postedWithoutPostalAddressData.rebuild() }
            .isInstanceOf(InconsistentInvitationDataException::class)
            .messageContains("A posted invitation must include a postal address.")
    }

    @Test
    fun `should keep the domain error as cause`() {
        assertFailure { postedWithoutPostalAddressData.rebuild() }
            .prop(Throwable::cause)
            .isNotNull()
            .isInstanceOf(IllegalArgumentException::class)
    }
}
