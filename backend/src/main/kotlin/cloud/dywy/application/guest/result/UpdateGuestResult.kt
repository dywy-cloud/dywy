package cloud.dywy.application.guest.result

import cloud.dywy.domain.guest.entity.Guest

sealed interface UpdateGuestResult {
    data class Updated(val guest: Guest) : UpdateGuestResult
    data object NotFound : UpdateGuestResult
    data object VersionConflict : UpdateGuestResult
}

