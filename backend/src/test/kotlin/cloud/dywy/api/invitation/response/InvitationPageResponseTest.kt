package cloud.dywy.api.invitation.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.api.invitation.response.InvitationResponseFixtures.firstPage
import cloud.dywy.domain.invitation.entity.InvitationPageFixtures.firstPage as firstPageDomain
import kotlin.test.Test

class InvitationPageResponseTest {

    @Test
    fun `should map invitation page to response`() {
        val response = firstPageDomain.toResponse()

        assertThat(response).isEqualTo(firstPage)
    }
}

