package cloud.dywy.domain.guest.entity

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.domain.guest.entity.GuestMagicLinkFixtures.bridesMaidToJane
import cloud.dywy.domain.guest.entity.GuestMagicLinkFixtures.groupBridesMaidToJane
import kotlin.test.Test

class GuestMagicLinkTest {

    @Test
    fun `should build guest access path from magic-link`() {
        val path = bridesMaidToJane.guestAccessPath()

        assertThat(path).isEqualTo(
            "/api/guest-access/magic-links/53c2efcd-b4fc-42f3-a73b-fadf3725af3f"
        )
    }

    @Test
    fun `should be individual by default`() {
        assertThat(bridesMaidToJane.mode).isEqualTo(GuestMagicLinkMode.INDIVIDUAL)
    }

    @Test
    fun `should keep the same access path in group mode`() {
        assertThat(groupBridesMaidToJane.mode).isEqualTo(GuestMagicLinkMode.GROUP)
        assertThat(groupBridesMaidToJane.guestAccessPath()).isEqualTo(bridesMaidToJane.guestAccessPath())
    }
}

