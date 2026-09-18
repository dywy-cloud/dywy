package cloud.dywy.application.guest.result

import cloud.dywy.domain.guest.entity.Guest

sealed interface RestoreGuestResult {
    data class Restored(val guest: Guest) : RestoreGuestResult
    data object NotFound : RestoreGuestResult
    data object VersionConflict : RestoreGuestResult
}

