package cloud.dywy.domain.invitation.entity

import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.shared.Dates
import java.time.LocalDateTime

data class Invitation(
    val id: InvitationId = InvitationId.generate(),
    val version: Long = 1L,
    val creationDate: LocalDateTime = Dates.nowUtcMillis(),
    val updateDate: LocalDateTime = Dates.nowUtcMillis(),
    val label: String,
    val description: String,
    val guests: Set<Guest>,
    val accessToken: InvitationAccessToken = InvitationAccessToken.generate(),
) {
    init {
        require(guests.isNotEmpty()) { "An invitation must include at least one guest." }
    }
}