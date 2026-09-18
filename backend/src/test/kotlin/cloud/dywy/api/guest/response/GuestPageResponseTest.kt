package cloud.dywy.api.guest.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.domain.guest.entity.GuestFixtures.johnDoe
import cloud.dywy.domain.guest.entity.GuestPage
import kotlin.test.Test

class GuestPageResponseTest {

    @Test
    fun `should map guest page to response`() {
        val guestPage = GuestPage(
            items = listOf(johnDoe),
            page = 1,
            size = 5,
            totalItems = 9,
            totalPages = 2,
        )

        assertThat(guestPage.toResponse()).isEqualTo(
            GuestPageResponse(
                items = listOf(GuestResponseFixtures.johnDoe),
                page = 1,
                size = 5,
                totalItems = 9,
                totalPages = 2,
            )
        )
    }
}

