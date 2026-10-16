package cloud.dywy.domain.rsvp.entity

enum class RsvpAttendance {
    ATTENDING,
    DECLINED,
    PENDING;

    companion object {
        fun parse(value: String?): RsvpAttendance =
            value?.trim()?.uppercase()?.let { candidate -> entries.firstOrNull { it.name == candidate } } ?: PENDING
    }
}


