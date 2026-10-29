package cloud.dywy.application.invitation

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.domain.dashboard.entity.DashboardStatisticsFixtures
import cloud.dywy.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.friendsInvitation
import cloud.dywy.domain.invitation.entity.InvitationListCriteria
import cloud.dywy.domain.invitation.entity.InvitationPage
import cloud.dywy.domain.invitation.entity.InvitationSearch
import cloud.dywy.domain.invitation.repository.Invitations
import cloud.dywy.domain.rsvp.entity.RsvpStatus
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test

class InvitationListerTest {

    private lateinit var invitations: Invitations
    private lateinit var guestRsvps: GuestRsvps
    private lateinit var invitationLister: InvitationLister

    @BeforeTest
    fun setUp() {
        invitations = mockk()
        guestRsvps = mockk()
        invitationLister = InvitationLister(invitations, InvitationRsvpLoader(guestRsvps))
    }

    @Test
    fun `should list invitations with provided criteria`() {
        val criteria = InvitationListCriteria(page = 1, size = 5)
        val expectedPage = InvitationPage(
            items = listOf(brideFamilyInvitation),
            page = 1,
            size = 5,
            totalItems = 1,
            totalPages = 1,
        )

        every { invitations.list(criteria) } returns expectedPage

        assertThat(invitationLister.list(InvitationSearch(criteria))).isEqualTo(expectedPage)
    }

    @Test
    fun `should list invitations with default criteria`() {
        val expectedPage = InvitationPage(
            items = emptyList(),
            page = 0,
            size = 10,
            totalItems = 0,
            totalPages = 0,
        )

        every { invitations.list(InvitationListCriteria()) } returns expectedPage

        assertThat(invitationLister.list(InvitationSearch())).isEqualTo(expectedPage)
    }

    @Test
    fun `should only list invitations in the requested rsvp status`() {
        givenSeededInvitations()

        assertThat(invitationLister.list(InvitationSearch(rsvpStatus = RsvpStatus.ACCEPTED_ALL))).isEqualTo(
            InvitationPage(
                items = listOf(brideFamilyInvitation, bridesMaidInvitation),
                page = 0,
                size = 10,
                totalItems = 2,
                totalPages = 1,
            )
        )
        assertThat(invitationLister.list(InvitationSearch(rsvpStatus = RsvpStatus.PARTIALLY_ANSWERED)).items)
            .isEqualTo(listOf(friendsInvitation))
    }

    @Test
    fun `should list no invitation when none is in the requested rsvp status`() {
        givenSeededInvitations()

        assertThat(invitationLister.list(InvitationSearch(rsvpStatus = RsvpStatus.DECLINED_ALL))).isEqualTo(
            InvitationPage(items = emptyList(), page = 0, size = 10, totalItems = 0, totalPages = 0)
        )
    }

    @Test
    fun `should paginate the invitations in the requested rsvp status`() {
        givenSeededInvitations()

        assertThat(
            invitationLister.list(InvitationSearch(InvitationListCriteria(page = 1, size = 1), RsvpStatus.ACCEPTED_ALL))
        ).isEqualTo(
            InvitationPage(
                items = listOf(bridesMaidInvitation),
                page = 1,
                size = 1,
                totalItems = 2,
                totalPages = 2,
            )
        )
    }

    @Test
    fun `should combine the label with the requested rsvp status`() {
        every { invitations.list(InvitationListCriteria(page = 0, size = 100, label = "Bride")) } returns
            InvitationPage(items = listOf(brideFamilyInvitation), page = 0, size = 100, totalItems = 1, totalPages = 1)
        every { guestRsvps.findByGuestIds(any()) } returns DashboardStatisticsFixtures.rsvps

        assertThat(
            invitationLister.list(InvitationSearch(InvitationListCriteria(label = "Bride"), RsvpStatus.ACCEPTED_ALL)).items
        ).isEqualTo(listOf(brideFamilyInvitation))
        verify(exactly = 1) { invitations.list(InvitationListCriteria(page = 0, size = 100, label = "Bride")) }
    }

    @Test
    fun `should find the requested rsvp status page across several repository batches`() {
        every { invitations.list(InvitationListCriteria(page = 0, size = 100)) } returns
            InvitationPage(items = listOf(friendsInvitation), page = 0, size = 100, totalItems = 3, totalPages = 2)
        every { invitations.list(InvitationListCriteria(page = 1, size = 100)) } returns
            InvitationPage(
                items = listOf(brideFamilyInvitation, bridesMaidInvitation),
                page = 1,
                size = 100,
                totalItems = 3,
                totalPages = 2,
            )
        every { guestRsvps.findByGuestIds(any()) } returns DashboardStatisticsFixtures.rsvps

        assertThat(
            invitationLister.list(InvitationSearch(InvitationListCriteria(page = 1, size = 1), RsvpStatus.ACCEPTED_ALL))
        ).isEqualTo(
            InvitationPage(items = listOf(bridesMaidInvitation), page = 1, size = 1, totalItems = 2, totalPages = 2)
        )
    }

    private fun givenSeededInvitations() {
        every { invitations.list(InvitationListCriteria(page = 0, size = 100)) } returns
            InvitationPage(
                items = DashboardStatisticsFixtures.invitations,
                page = 0,
                size = 100,
                totalItems = 3,
                totalPages = 1,
            )
        every { guestRsvps.findByGuestIds(any()) } returns DashboardStatisticsFixtures.rsvps
    }
}
