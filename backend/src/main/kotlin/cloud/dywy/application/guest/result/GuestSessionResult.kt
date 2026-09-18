package cloud.dywy.application.guest.result

import cloud.dywy.domain.guest.entity.Guest

sealed interface GuestSessionResult {

    data class Resolved(val guest: Guest) : GuestSessionResult

    data object InvitationNotFound : GuestSessionResult

    data object GuestNotInInvitation : GuestSessionResult
}

