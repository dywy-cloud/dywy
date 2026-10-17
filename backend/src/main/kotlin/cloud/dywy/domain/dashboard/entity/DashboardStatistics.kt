package cloud.dywy.domain.dashboard.entity

import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.rsvp.entity.GuestRsvp
import cloud.dywy.domain.rsvp.entity.Meal
import cloud.dywy.domain.rsvp.entity.RsvpAttendance
import cloud.dywy.domain.rsvp.entity.RsvpStatus
import cloud.dywy.domain.rsvp.entity.RsvpSummary

data class DashboardStatistics(
    val totalInvitations: Int,
    val totalGuests: Int,
    val invitationsByRsvpStatus: Map<RsvpStatus, Int>,
    val guestsByAttendance: Map<RsvpAttendance, Int>,
    val mealCounts: Map<Meal, Int>,
    val deliveryMethodCounts: Map<DeliveryMethod, Int>,
) {
    companion object {

        fun of(invitations: Collection<Invitation>, rsvps: Collection<GuestRsvp>): DashboardStatistics {
            val guestIds = invitations.flatMap { invitation -> invitation.guests.map { it.id } }.toSet()
            val guestRsvps = rsvps.filter { it.guestId in guestIds }
            val summaries = invitations.map { invitation ->
                RsvpSummary.of(invitation.guests.map { it.id }, guestRsvps)
            }
            val attendances = guestIds.associateWith { RsvpAttendance.PENDING } +
                guestRsvps.associate { it.guestId to it.attendance }

            return DashboardStatistics(
                totalInvitations = invitations.size,
                totalGuests = guestIds.size,
                invitationsByRsvpStatus = countAll(RsvpStatus.entries, summaries.map { it.status }),
                guestsByAttendance = countAll(RsvpAttendance.entries, attendances.values),
                mealCounts = countAll(Meal.entries, guestRsvps.mapNotNull { it.answers?.meal }),
                deliveryMethodCounts = countAll(DeliveryMethod.entries, invitations.mapNotNull { it.deliveryMethod }),
            )
        }

        private fun <T> countAll(keys: Collection<T>, values: Collection<T>): Map<T, Int> =
            keys.associateWith { key -> values.count { it == key } }
    }
}

