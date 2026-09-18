package cloud.dywy.application.guest

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.guest.repository.Guests
import org.springframework.stereotype.Service

@Service
class GuestGetter(private val guests: Guests) {
    fun get(id: GuestId) = guests.findById(id)
}

