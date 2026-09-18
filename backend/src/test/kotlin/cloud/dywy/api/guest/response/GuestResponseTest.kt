package cloud.dywy.api.guest.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.domain.guest.entity.GuestFixtures.johnDoe
import kotlin.test.Test

class GuestResponseTest {

    @Test
    fun `should map guest to response`() {
            assertThat(johnDoe.toResponse()).isEqualTo(GuestResponseFixtures.johnDoe)
    }
}


