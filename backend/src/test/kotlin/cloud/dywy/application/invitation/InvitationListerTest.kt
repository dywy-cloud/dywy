package cloud.dywy.application.invitation

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.friendsInvitation
import cloud.dywy.domain.invitation.entity.InvitationListCriteria
import cloud.dywy.domain.invitation.entity.InvitationPage
import cloud.dywy.domain.invitation.entity.InvitationSearch
import cloud.dywy.domain.invitation.repository.Invitations
import cloud.dywy.domain.rsvp.entity.RsvpStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test

class InvitationListerTest {

    private lateinit var invitations: Invitations
    private lateinit var invitationLister: InvitationLister

    @BeforeTest
    fun setUp() {
        invitations = mockk()
        invitationLister = InvitationLister(invitations)
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
        every { invitations.listByRsvpStatus(any(), RsvpStatus.ACCEPTED_ALL) } returns
            InvitationPage(
                items = listOf(brideFamilyInvitation, bridesMaidInvitation),
                page = 0,
                size = 10,
                totalItems = 2,
                totalPages = 1,
            )
        every { invitations.listByRsvpStatus(any(), RsvpStatus.PARTIALLY_ANSWERED) } returns
            InvitationPage(
                items = listOf(friendsInvitation),
                page = 0,
                size = 10,
                totalItems = 1,
                totalPages = 1,
            )

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
        every { invitations.listByRsvpStatus(any(), RsvpStatus.DECLINED_ALL) } returns
            InvitationPage(items = emptyList(), page = 0, size = 10, totalItems = 0, totalPages = 0)

        assertThat(invitationLister.list(InvitationSearch(rsvpStatus = RsvpStatus.DECLINED_ALL))).isEqualTo(
            InvitationPage(items = emptyList(), page = 0, size = 10, totalItems = 0, totalPages = 0)
        )
    }

    @Test
    fun `should paginate the invitations in the requested rsvp status`() {
        every { invitations.listByRsvpStatus(InvitationListCriteria(page = 1, size = 1), RsvpStatus.ACCEPTED_ALL) } returns
            InvitationPage(
                items = listOf(bridesMaidInvitation),
                page = 1,
                size = 1,
                totalItems = 2,
                totalPages = 2,
            )

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
        every { invitations.listByRsvpStatus(InvitationListCriteria(label = "Bride"), RsvpStatus.ACCEPTED_ALL) } returns
            InvitationPage(items = listOf(brideFamilyInvitation), page = 0, size = 10, totalItems = 1, totalPages = 1)

        assertThat(
            invitationLister.list(InvitationSearch(InvitationListCriteria(label = "Bride"), RsvpStatus.ACCEPTED_ALL)).items
        ).isEqualTo(listOf(brideFamilyInvitation))
        verify(exactly = 1) { invitations.listByRsvpStatus(InvitationListCriteria(label = "Bride"), RsvpStatus.ACCEPTED_ALL) }
    }
}
