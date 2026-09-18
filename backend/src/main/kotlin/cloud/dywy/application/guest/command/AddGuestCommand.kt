package cloud.dywy.application.guest.command

import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.Language

data class AddGuestCommand(
    val firstName: String,
    val lastName: String,
    val email: String,
    val language: Language = Language.FR,
) {
    fun toGuest() =
        Guest(
            firstName = firstName,
            lastName = lastName,
            email = email,
            language = language,
        )
}

