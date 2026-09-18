package cloud.dywy.application.guest

import cloud.dywy.application.guest.command.UpdateGuestCommand
import cloud.dywy.application.guest.result.UpdateGuestResult
import cloud.dywy.domain.guest.repository.Guests
import org.springframework.stereotype.Service

@Service
class GuestUpdater(private val guests: Guests) {

    fun update(command: UpdateGuestCommand): UpdateGuestResult =
        with(command) {
            guests.findById(id)
                ?.let { existingGuest ->
                    when {
                        existingGuest.version != version -> UpdateGuestResult.VersionConflict
                        else -> existingGuest
                            .updateDetails(firstName, lastName, email, language ?: existingGuest.language)
                            .let { guests.update(it, expectedVersion = version) }
                            ?.let(UpdateGuestResult::Updated)
                            ?: UpdateGuestResult.VersionConflict
                    }
                }
                ?: UpdateGuestResult.NotFound
        }
}


