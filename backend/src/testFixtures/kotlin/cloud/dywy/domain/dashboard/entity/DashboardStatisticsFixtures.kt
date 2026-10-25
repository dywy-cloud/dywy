package cloud.dywy.domain.dashboard.entity

import cloud.dywy.domain.guest.entity.GuestFixtures.emmaWilson
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.friendsInvitation
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.janeDoeRsvpWithSameSyncedChoicesAsJohnDoe
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpWithChoices
import cloud.dywy.domain.rsvp.entity.Meal
import cloud.dywy.domain.rsvp.entity.RsvpAttendance
import cloud.dywy.domain.rsvp.entity.RsvpStatus

object DashboardStatisticsFixtures {

    val invitations = listOf(brideFamilyInvitation, bridesMaidInvitation, friendsInvitation)

    val emmaWilsonRsvpDeclined = johnDoeRsvp.copy(guestId = emmaWilson.id, attendance = RsvpAttendance.DECLINED)

    val rsvps = listOf(johnDoeRsvpWithChoices, janeDoeRsvpWithSameSyncedChoicesAsJohnDoe, emmaWilsonRsvpDeclined)

    val statistics = DashboardStatistics(
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
        deliveryMethodCounts = mapOf(DeliveryMethod.HAND_DELIVERED to 1, DeliveryMethod.POSTED to 2),
    )
}
