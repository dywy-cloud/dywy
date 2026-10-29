package cloud.dywy.application.dashboard

import cloud.dywy.application.invitation.InvitationRsvpLoader
import cloud.dywy.domain.dashboard.entity.DashboardStatistics
import cloud.dywy.domain.invitation.repository.Invitations
import cloud.dywy.domain.invitation.repository.listAll
import org.springframework.stereotype.Service

@Service
class DashboardStatisticsGetter(
    private val invitations: Invitations,
    private val invitationRsvpLoader: InvitationRsvpLoader,
) {

    fun get(): DashboardStatistics =
        invitations.listAll().let { allInvitations ->
            DashboardStatistics.of(
                invitations = allInvitations,
                rsvps = invitationRsvpLoader.load(allInvitations).values,
            )
        }
}
