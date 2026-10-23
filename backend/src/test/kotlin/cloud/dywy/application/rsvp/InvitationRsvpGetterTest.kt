package cloud.dywy.application.rsvp

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.application.rsvp.result.GetInvitationRsvpsResult
import cloud.dywy.domain.invitation.entity.InvitationFixtures.martinFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.rsvp.entity.InvitationGuestRsvpFixtures.lucasMartinPending
import cloud.dywy.domain.rsvp.entity.InvitationGuestRsvpFixtures.sofiaMartinAnswered
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test

class InvitationRsvpGetterTest {

    private lateinit var guestRsvps: GuestRsvps
    private lateinit var invitationRsvpGetter: InvitationRsvpGetter

    @BeforeTest
    fun setUp() {
        guestRsvps = mockk()
        invitationRsvpGetter = InvitationRsvpGetter(guestRsvps)
    }

    @Test
    fun `should return every guest of the invitation with the rsvp only when submitted`() {
        every { guestRsvps.findInvitationGuestRsvps(martinFamilyInvitation.id) } returns
            listOf(sofiaMartinAnswered, lucasMartinPending)

        val result = invitationRsvpGetter.get(martinFamilyInvitation.id)

        assertThat(result).isEqualTo(GetInvitationRsvpsResult.Found(listOf(sofiaMartinAnswered, lucasMartinPending)))
    }

    @Test
    fun `should report an unknown invitation`() {
        val unknownInvitationId = InvitationId.generate()
        every { guestRsvps.findInvitationGuestRsvps(unknownInvitationId) } returns emptyList()

        assertThat(invitationRsvpGetter.get(unknownInvitationId)).isEqualTo(GetInvitationRsvpsResult.InvitationNotFound)
    }
}

