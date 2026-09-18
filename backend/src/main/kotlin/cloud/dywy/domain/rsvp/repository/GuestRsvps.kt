package cloud.dywy.domain.rsvp.repository

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.rsvp.entity.GuestRsvp

interface GuestRsvps {

    fun findByGuestId(guestId: GuestId): GuestRsvp?
    fun save(rsvp: GuestRsvp): GuestRsvp
    fun isSongOnPlaylist(deezerId: Long): Boolean
    fun isSongChosenByAnyGuest(deezerId: Long): Boolean
    fun findGuestsWithUnsynchronizedSong(): List<PendingSongSync>
    fun markSongSynchronized(guestId: GuestId)
}

