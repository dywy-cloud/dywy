package cloud.dywy.application.rsvp

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.application.rsvp.result.GetInvitationRsvpsResult
import cloud.dywy.domain.guest.entity.GuestFixtures.sofiaMartin
import cloud.dywy.domain.guest.entity.GuestFixtures.lucasMartin
import cloud.dywy.domain.invitation.entity.InvitationFixtures.martinFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.invitation.repository.Invitations
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.sofiaMartinRsvp
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test

class InvitationRsvpGetterTest {

    private lateinit var invitations: Invitations
    private lateinit var guestRsvps: GuestRsvps
    private lateinit var invitationRsvpGetter: InvitationRsvpGetter

    @BeforeTest
    fun setUp() {
        invitations = mockk()
        guestRsvps = mockk()
        invitationRsvpGetter = InvitationRsvpGetter(invitations, guestRsvps)
    }

    @Test
    fun `should return the submitted rsvps of the invitation guests`() {
        every { invitations.findById(martinFamilyInvitation.id) } returns martinFamilyInvitation
        every { guestRsvps.findByGuestIds(setOf(sofiaMartin.id, lucasMartin.id)) } returns listOf(sofiaMartinRsvp)

        val result = invitationRsvpGetter.get(martinFamilyInvitation.id)

        assertThat(result).isEqualTo(GetInvitationRsvpsResult.Found(listOf(sofiaMartinRsvp)))
    }

    @Test
    fun `should report an unknown invitation`() {
        val unknownInvitationId = InvitationId.generate()
        every { invitations.findById(unknownInvitationId) } returns null

        assertThat(invitationRsvpGetter.get(unknownInvitationId)).isEqualTo(GetInvitationRsvpsResult.InvitationNotFound)
    }
}

