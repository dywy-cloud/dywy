package cloud.dywy.api.invitation.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.api.invitation.response.InvitationResponseFixtures.brideFamilyPublic
import cloud.dywy.api.invitation.response.InvitationResponseFixtures.friendsPublic
import cloud.dywy.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.friendsInvitation
import kotlin.test.Test

class PublicInvitationResponseTest {

    @Test
    fun `should map invitation to public response`() {
        val response = brideFamilyInvitation.toPublicResponse()

        assertThat(response).isEqualTo(brideFamilyPublic)
    }

    @Test
    fun `should sort guests by id when mapping public response`() {
        val response = friendsInvitation.toPublicResponse()

        assertThat(response).isEqualTo(friendsPublic)
    }
}

