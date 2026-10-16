package cloud.dywy.application.invitation

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.friendsInvitation
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test

class InvitationRsvpLoaderTest {

    private lateinit var guestRsvps: GuestRsvps
    private lateinit var invitationRsvpLoader: InvitationRsvpLoader

    @BeforeTest
    fun setUp() {
        guestRsvps = mockk()
        invitationRsvpLoader = InvitationRsvpLoader(guestRsvps)
    }

    @Test
    fun `should load the rsvps of every invitation guest in a single lookup`() {
        val guestIds = (brideFamilyInvitation.guests + friendsInvitation.guests).map { it.id }.toSet()

        every { guestRsvps.findByGuestIds(guestIds) } returns listOf(johnDoeRsvp)

        assertThat(invitationRsvpLoader.load(listOf(brideFamilyInvitation, friendsInvitation)))
            .isEqualTo(mapOf(johnDoeRsvp.guestId to johnDoeRsvp))
        verify(exactly = 1) { guestRsvps.findByGuestIds(any()) }
    }
}
