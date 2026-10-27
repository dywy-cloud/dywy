package cloud.dywy.api.dashboard.response

import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.rsvp.entity.Meal
import cloud.dywy.domain.rsvp.entity.RsvpAttendance
import cloud.dywy.domain.rsvp.entity.RsvpStatus

object DashboardStatisticsResponseFixtures {
    private val deliveryMethodCounts = mapOf(DeliveryMethod.HAND_DELIVERED to 1, DeliveryMethod.POSTED to 2)

    val statisticsResponse = DashboardStatisticsResponse(
        totalInvitations = 3,
        totalGuests = 4,
        invitationsByRsvpStatus = mapOf(
            RsvpStatus.ACCEPTED_ALL to 2,
            RsvpStatus.DECLINED_ALL to 0,
            RsvpStatus.PARTIALLY_ACCEPTED to 0,
            RsvpStatus.PARTIALLY_ANSWERED to 1,
            RsvpStatus.PENDING to 0,
        ),
        guestsByAttendance = mapOf(
            RsvpAttendance.ATTENDING to 2,
            RsvpAttendance.DECLINED to 1,
            RsvpAttendance.PENDING to 1,
        ),
        mealCounts = mapOf(Meal.MEAT to 1, Meal.FISH to 1, Meal.VEGGIE to 0),
        deliveryMethodCounts = deliveryMethodCounts,
    )

    val seededDashboardStatistics = DashboardStatisticsResponse(
        totalInvitations = 3,
        totalGuests = 4,
        invitationsByRsvpStatus = mapOf(
            RsvpStatus.ACCEPTED_ALL to 1,
            RsvpStatus.DECLINED_ALL to 1,
            RsvpStatus.PARTIALLY_ACCEPTED to 1,
            RsvpStatus.PARTIALLY_ANSWERED to 0,
            RsvpStatus.PENDING to 0,
        ),
        guestsByAttendance = mapOf(
            RsvpAttendance.ATTENDING to 2,
            RsvpAttendance.DECLINED to 2,
            RsvpAttendance.PENDING to 0,
        ),
        mealCounts = mapOf(Meal.MEAT to 1, Meal.FISH to 1, Meal.VEGGIE to 0),
        deliveryMethodCounts = deliveryMethodCounts,
    )
}
