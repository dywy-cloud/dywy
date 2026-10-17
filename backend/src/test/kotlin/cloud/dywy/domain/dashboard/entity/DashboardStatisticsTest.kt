package cloud.dywy.domain.dashboard.entity

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.domain.dashboard.entity.DashboardStatisticsFixtures.invitations
import cloud.dywy.domain.dashboard.entity.DashboardStatisticsFixtures.rsvps
import cloud.dywy.domain.dashboard.entity.DashboardStatisticsFixtures.statistics
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import cloud.dywy.domain.rsvp.entity.Meal
import cloud.dywy.domain.rsvp.entity.RsvpAttendance
import cloud.dywy.domain.rsvp.entity.RsvpStatus
import kotlin.test.Test

class DashboardStatisticsTest {

    @Test
    fun `should aggregate invitations, guests, meals and delivery methods`() {
        assertThat(DashboardStatistics.of(invitations, rsvps)).isEqualTo(statistics)
    }

    @Test
    fun `should count every enum value even without data`() {
        val empty = DashboardStatistics.of(emptyList(), emptyList())

        assertThat(empty.totalInvitations).isEqualTo(0)
        assertThat(empty.totalGuests).isEqualTo(0)
        assertThat(empty.invitationsByRsvpStatus.keys).isEqualTo(RsvpStatus.entries.toSet())
        assertThat(empty.guestsByAttendance.keys).isEqualTo(RsvpAttendance.entries.toSet())
        assertThat(empty.mealCounts.keys).isEqualTo(Meal.entries.toSet())
        assertThat(empty.deliveryMethodCounts.keys).isEqualTo(DeliveryMethod.entries.toSet())
    }

    @Test
    fun `should count guests without rsvp as pending`() {
        val pendingOnly = DashboardStatistics.of(listOf(brideFamilyInvitation), emptyList())

        assertThat(pendingOnly.guestsByAttendance[RsvpAttendance.PENDING]).isEqualTo(1)
        assertThat(pendingOnly.invitationsByRsvpStatus[RsvpStatus.PENDING]).isEqualTo(1)
    }

    @Test
    fun `should ignore rsvps of guests without invitation`() {
        val unrelated = DashboardStatistics.of(emptyList(), rsvps)

        assertThat(unrelated.mealCounts.values.sum()).isEqualTo(0)
        assertThat(unrelated.guestsByAttendance.values.sum()).isEqualTo(0)
    }
}


