package cloud.dywy.infrastructure.guest.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.guest.entity.GuestMagicLinkFixtures.bridesMaidToJane
import kotlin.test.BeforeTest
import kotlin.test.Test

class NoopGuestMagicLinkSenderTest {

    private lateinit var noopGuestMagicLinkSender: NoopGuestMagicLinkSender

    @BeforeTest
    fun setUp() {
        noopGuestMagicLinkSender = NoopGuestMagicLinkSender()
    }

    @Test
    fun `should do nothing when sending magic link`() {
        assertThat(noopGuestMagicLinkSender.send(bridesMaidToJane, janeDoe)).isEqualTo(Unit)
    }
}


