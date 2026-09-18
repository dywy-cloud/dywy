package cloud.dywy.application.guest

import cloud.dywy.application.guest.command.AddGuestCommand
import cloud.dywy.domain.guest.repository.Guests
import org.springframework.stereotype.Service

@Service
class GuestAdder(private val guests: Guests) {
    fun add(command: AddGuestCommand) =
        guests.add(command.toGuest())
}
