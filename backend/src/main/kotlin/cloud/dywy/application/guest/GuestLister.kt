package cloud.dywy.application.guest

import cloud.dywy.domain.guest.entity.GuestListCriteria
import cloud.dywy.domain.guest.repository.Guests
import org.springframework.stereotype.Service

@Service
class GuestLister(private val guests: Guests) {
    fun list(criteria: GuestListCriteria) = guests.list(criteria)
}

