package cloud.dywy.application.rsvp

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import cloud.dywy.application.rsvp.result.GetGuestRsvpResult
import cloud.dywy.domain.guest.entity.GuestFixtures.johnDoe
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestRsvpGetterTest {

    private lateinit var guestRsvps: GuestRsvps
    private lateinit var guestRsvpGetter: GuestRsvpGetter

    @BeforeTest
    fun setUp() {
        guestRsvps = mockk()
        guestRsvpGetter = GuestRsvpGetter(guestRsvps)
    }

    @Test
    fun `should return submitted when the guest has an rsvp`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns johnDoeRsvp

        assertThat(guestRsvpGetter.get(johnDoe.id)).isEqualTo(GetGuestRsvpResult.Submitted(johnDoeRsvp))
    }

    @Test
    fun `should return not submitted when the guest has no rsvp`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns null

        assertThat(guestRsvpGetter.get(johnDoe.id)).isEqualTo(GetGuestRsvpResult.NotSubmittedYet)
    }
}

