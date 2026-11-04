package cloud.dywy.api.invitation.request

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import cloud.dywy.api.common.ServerRequestTestHelper.requestWithParams
import cloud.dywy.domain.invitation.entity.InvitationListCriteria
import cloud.dywy.domain.invitation.entity.InvitationSearch
import cloud.dywy.domain.rsvp.entity.RsvpStatus
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.Test

class InvitationSearchExtensionsTest {

    @Test
    fun `should build the default invitation search when no query param is given`() {
        val request = requestWithParams()

        assertThat(request.invitationSearchOrNull()).isEqualTo(InvitationSearch())
    }

    @Test
    fun `should build the invitation search from every query param`() {
        val request = requestWithParams("page" to "2", "size" to "5", "label" to "  family ", "rsvpStatus" to "pending")

        assertThat(request.invitationSearchOrNull()).isEqualTo(
            InvitationSearch(
                criteria = InvitationListCriteria(page = 2, size = 5, label = "family"),
                rsvpStatus = RsvpStatus.PENDING,
            )
        )
    }

    @ParameterizedTest
    @CsvSource(
        "page, -1",
        "page, abc",
        "size, 0",
        "size, abc",
        "rsvpStatus, MAYBE",
    )
    fun `should return no invitation search when a query param is invalid`(name: String, value: String) {
        assertThat(requestWithParams(name to value).invitationSearchOrNull()).isNull()
    }
}
