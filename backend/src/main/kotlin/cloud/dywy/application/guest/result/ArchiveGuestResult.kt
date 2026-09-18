package cloud.dywy.application.guest.result

import cloud.dywy.domain.guest.entity.Guest

sealed interface ArchiveGuestResult {
    data class Archived(val guest: Guest) : ArchiveGuestResult
    data object NotFound : ArchiveGuestResult
    data object VersionConflict : ArchiveGuestResult
}