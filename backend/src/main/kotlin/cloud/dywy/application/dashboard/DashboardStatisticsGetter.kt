package cloud.dywy.application.dashboard

import cloud.dywy.domain.dashboard.entity.DashboardStatistics
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.InvitationListCriteria
import cloud.dywy.domain.invitation.repository.Invitations
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import org.springframework.stereotype.Service

@Service
class DashboardStatisticsGetter(
    private val invitations: Invitations,
    private val guestRsvps: GuestRsvps,
) {

    fun get(): DashboardStatistics =
        allInvitations().let { allInvitations ->
            DashboardStatistics.of(
                invitations = allInvitations,
                rsvps = guestRsvps.findByGuestIds(guestIdsOf(allInvitations)),
            )
        }

    private fun allInvitations(): List<Invitation> =
        generateSequence(invitations.list(InvitationListCriteria(page = 0, size = PAGE_SIZE))) { page ->
            if (page.page + 1 < page.totalPages) {
                invitations.list(InvitationListCriteria(page = page.page + 1, size = PAGE_SIZE))
            } else {
                null
            }
        }.flatMap { it.items }.toList()

    private fun guestIdsOf(invitations: Collection<Invitation>): Set<GuestId> =
        invitations.flatMap { invitation -> invitation.guests.map { it.id } }.toSet()

    private companion object {
        const val PAGE_SIZE = 100
    }
}

