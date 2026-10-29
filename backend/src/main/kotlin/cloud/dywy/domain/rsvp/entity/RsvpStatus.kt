package cloud.dywy.domain.rsvp.entity

enum class RsvpStatus {
    ACCEPTED_ALL,
    DECLINED_ALL,
    PARTIALLY_ACCEPTED,
    PARTIALLY_ANSWERED,
    PENDING;

    companion object {
        fun parse(value: String?): RsvpStatus? =
            value?.trim()?.uppercase()?.let { candidate -> entries.firstOrNull { it.name == candidate } }
    }
}
