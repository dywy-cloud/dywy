package cloud.dywy.application.guest.command

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.guest.entity.Language

data class UpdateGuestCommand(
    val id: GuestId,
    val version: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val language: Language? = null,
)

