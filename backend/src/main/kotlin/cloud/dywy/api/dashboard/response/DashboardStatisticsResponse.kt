package cloud.dywy.api.dashboard.response

import cloud.dywy.domain.dashboard.entity.DashboardStatistics
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.rsvp.entity.Meal
import cloud.dywy.domain.rsvp.entity.RsvpAttendance
import cloud.dywy.domain.rsvp.entity.RsvpStatus

data class DashboardStatisticsResponse(
    val totalInvitations: Int,
    val totalGuests: Int,
    val invitationsByRsvpStatus: Map<RsvpStatus, Int>,
    val guestsByAttendance: Map<RsvpAttendance, Int>,
    val mealCounts: Map<Meal, Int>,
    val deliveryMethodCounts: Map<DeliveryMethod, Int>,
)

internal fun DashboardStatistics.toResponse() = DashboardStatisticsResponse(
    totalInvitations = totalInvitations,
    totalGuests = totalGuests,
    invitationsByRsvpStatus = invitationsByRsvpStatus,
    guestsByAttendance = guestsByAttendance,
    mealCounts = mealCounts,
    deliveryMethodCounts = deliveryMethodCounts,
)
