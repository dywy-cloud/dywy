package cloud.dywy.support

import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.rsvp.entity.GuestRsvp
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import cloud.dywy.infrastructure.rsvp.repository.GuestRsvpExposedRepository
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class FailingGuestRsvpsConfig {

    @Bean
    @Primary
    fun failingGuestRsvps(delegate: GuestRsvpExposedRepository) = FailingGuestRsvps(delegate)
}

class FailingGuestRsvps(private val delegate: GuestRsvps) : GuestRsvps by delegate {
    private var failingGuestId: GuestId? = null

    override fun save(rsvp: GuestRsvp): GuestRsvp =
        delegate.save(
            rsvp.takeIf { it.guestId != failingGuestId }
                ?: error("Simulated failure while saving the rsvp of ${rsvp.guestId}")
        )

    fun failSaving(guestId: GuestId) {
        failingGuestId = guestId
    }

    fun reset() {
        failingGuestId = null
    }
}

