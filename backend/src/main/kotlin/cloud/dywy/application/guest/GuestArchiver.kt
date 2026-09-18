package cloud.dywy.application.guest

import cloud.dywy.application.guest.result.ArchiveGuestResult
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.guest.repository.Guests
import org.springframework.stereotype.Service

@Service
class GuestArchiver(private val guests: Guests) {

    fun archive(id: GuestId): ArchiveGuestResult =
        guests.findById(id)
            ?.let { existingGuest ->
                existingGuest.markAsArchived()
                    .let { guests.update(it, expectedVersion = existingGuest.version) }
                    ?.let(ArchiveGuestResult::Archived)
                    ?: ArchiveGuestResult.VersionConflict
            }
            ?: ArchiveGuestResult.NotFound
}

