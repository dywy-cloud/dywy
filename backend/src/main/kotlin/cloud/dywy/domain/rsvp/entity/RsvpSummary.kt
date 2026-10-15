package cloud.dywy.domain.rsvp.entity

import cloud.dywy.domain.guest.entity.GuestId

data class RsvpSummary(
    val status: RsvpStatus,
    val attending: Int,
    val declined: Int,
    val pending: Int,
    val total: Int,
) {
    companion object {

        fun of(guestIds: Collection<GuestId>, rsvps: Collection<GuestRsvp>): RsvpSummary =
            guestIds.toSet().let { ids ->
                summarize(
                    total = ids.size,
                    attendances = rsvps.filter { it.guestId in ids }.associate { it.guestId to it.attendance }.values,
                )
            }
        private fun summarize(total: Int, attendances: Collection<RsvpAttendance>): RsvpSummary =
            RsvpSummary(
                status = statusOf(
                    attending = attendances.count { it == RsvpAttendance.ATTENDING },
                    declined = attendances.count { it == RsvpAttendance.DECLINED },
                    total = total,
                ),
                attending = attendances.count { it == RsvpAttendance.ATTENDING },
                declined = attendances.count { it == RsvpAttendance.DECLINED },
                pending = total - attendances.size,
                total = total,
            )
        private fun statusOf(attending: Int, declined: Int, total: Int): RsvpStatus = when {
            attending == 0 && declined == 0 -> RsvpStatus.PENDING
            attending + declined < total -> RsvpStatus.PARTIALLY_ANSWERED
            declined == 0 -> RsvpStatus.ACCEPTED_ALL
            attending == 0 -> RsvpStatus.DECLINED_ALL
            else -> RsvpStatus.PARTIALLY_ACCEPTED
        }
    }
}



