package cloud.dywy.application.dashboard

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.domain.dashboard.entity.DashboardStatisticsFixtures.invitations
import cloud.dywy.domain.dashboard.entity.DashboardStatisticsFixtures.rsvps
import cloud.dywy.domain.dashboard.entity.DashboardStatisticsFixtures.statistics
import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.InvitationListCriteria
import cloud.dywy.domain.invitation.entity.InvitationPage
import cloud.dywy.domain.invitation.repository.Invitations
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test

class DashboardStatisticsGetterTest {

    private lateinit var invitationRepository: Invitations
    private lateinit var guestRsvps: GuestRsvps
    private lateinit var dashboardStatisticsGetter: DashboardStatisticsGetter

    @BeforeTest
    fun setUp() {
        invitationRepository = mockk()
        guestRsvps = mockk()
        dashboardStatisticsGetter = DashboardStatisticsGetter(invitationRepository, guestRsvps)
    }

    @Test
    fun `should compute statistics over every invitation page`() {
        every { invitationRepository.list(InvitationListCriteria(page = 0, size = 100)) } returns
            invitationPage(items = invitations.take(2), page = 0)
        every { invitationRepository.list(InvitationListCriteria(page = 1, size = 100)) } returns
            invitationPage(items = invitations.drop(2), page = 1)
        every { guestRsvps.findByGuestIds(any()) } returns rsvps

        assertThat(dashboardStatisticsGetter.get()).isEqualTo(statistics)
        verify(exactly = 1) { guestRsvps.findByGuestIds(invitations.flatMap { it.guests }.map { it.id }.toSet()) }
    }

    private fun invitationPage(items: List<Invitation>, page: Int) = InvitationPage(
        items = items,
        page = page,
        size = 100,
        totalItems = invitations.size.toLong(),
        totalPages = 2,
    )
}
