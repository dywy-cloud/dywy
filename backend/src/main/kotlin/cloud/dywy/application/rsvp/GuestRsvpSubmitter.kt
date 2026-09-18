package cloud.dywy.application.rsvp

import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommand
import cloud.dywy.application.rsvp.command.toGuestRsvp
import cloud.dywy.application.rsvp.result.SubmitGuestRsvpResult
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.rsvp.entity.GuestRsvp
import cloud.dywy.domain.rsvp.entity.RsvpAnswers
import cloud.dywy.domain.rsvp.entity.SongChoice
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import org.springframework.stereotype.Service

@Service
class GuestRsvpSubmitter(
    private val guestRsvps: GuestRsvps,
    private val playlistSynchronizer: PlaylistSynchronizer,
) {

    fun submit(command: SubmitGuestRsvpCommand): SubmitGuestRsvpResult {
        val existing = guestRsvps.findByGuestId(command.guestId)
        val previousSong = existing?.answers?.song
        val result = if (existing != null) {
            val answers = command.answers.withPreservedSongSync(previousSong)
            SubmitGuestRsvpResult.Updated(guestRsvps.save(existing.respond(command.attendance, answers)))
        } else {
            SubmitGuestRsvpResult.Created(guestRsvps.save(command.toGuestRsvp()))
        }

        syncChosenSong(command.guestId, result.rsvp)
        unsyncRemovedSong(previousSong, result.rsvp.answers?.song)
        return result
    }

    private fun RsvpAnswers?.withPreservedSongSync(previousSong: SongChoice?): RsvpAnswers? {
        val song = this?.song ?: return this
        if (song.deezerId != previousSong?.deezerId) return this

        return copy(song = song.copy(synchronized = previousSong.synchronized))
    }

    // The RSVP is committed; push the song to the shared playlist when its sync is still pending.
    // Runs asynchronously (best-effort, isolated): a failure leaves it pending for the reconciler,
    // and the Deezer call never adds latency to the guest's (already saved) response.
    private fun syncChosenSong(guestId: GuestId, saved: GuestRsvp) {
        val song = saved.answers?.song ?: return
        if (song.synchronized) return

        playlistSynchronizer.syncAsync(guestId, song)
    }

    // When a previously-synced song is dropped (removed, replaced or the guest declined), ask the
    // synchronizer to remove it from the shared playlist — it keeps the track if another guest still
    // chose it. Only synced songs need removal; a still-pending one was never added. Runs async.
    private fun unsyncRemovedSong(previousSong: SongChoice?, currentSong: SongChoice?) {
        if (previousSong == null || !previousSong.synchronized) return
        if (previousSong.deezerId == currentSong?.deezerId) return

        playlistSynchronizer.unsyncAsync(previousSong.deezerId)
    }
}

