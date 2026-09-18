package cloud.dywy.api.invitation.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.api.invitation.response.InvitationResponseFixtures.brideFamily
import cloud.dywy.api.invitation.response.InvitationResponseFixtures.friends
import cloud.dywy.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.friendsInvitation
import kotlin.test.Test

class InvitationResponseTest {

    @Test
    fun `should map invitation to response`() {
        val response = brideFamilyInvitation.toResponse()

        assertThat(response).isEqualTo(brideFamily)
    }

    @Test
    fun `should sort guests by id when mapping invitation`() {
        val response = friendsInvitation.toResponse()

        assertThat(response).isEqualTo(friends)
    }
}

