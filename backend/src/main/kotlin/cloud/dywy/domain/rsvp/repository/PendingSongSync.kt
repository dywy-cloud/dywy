package cloud.dywy.domain.rsvp.repository

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.rsvp.entity.SongChoice

data class PendingSongSync(
    val guestId: GuestId,
    val song: SongChoice,
)

