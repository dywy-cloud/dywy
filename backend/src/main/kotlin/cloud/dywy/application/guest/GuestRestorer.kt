package cloud.dywy.application.guest

import cloud.dywy.application.guest.result.RestoreGuestResult
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.guest.repository.Guests
import org.springframework.stereotype.Service

@Service
class GuestRestorer(private val guests: Guests) {

    fun restore(id: GuestId): RestoreGuestResult =
        guests.findArchivedById(id)
            ?.let { existingGuest ->
                existingGuest.restore()
                    .let { guests.restore(it, expectedVersion = existingGuest.version) }
                    ?.let(RestoreGuestResult::Restored)
                    ?: RestoreGuestResult.VersionConflict
            }
            ?: RestoreGuestResult.NotFound
}

