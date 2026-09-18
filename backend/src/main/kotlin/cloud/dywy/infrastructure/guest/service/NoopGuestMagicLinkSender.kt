package cloud.dywy.infrastructure.guest.service

import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.GuestMagicLink
import cloud.dywy.domain.guest.service.GuestMagicLinkSender
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "app.mail", name = ["provider"], havingValue = "noop")
class NoopGuestMagicLinkSender : GuestMagicLinkSender {

    override fun send(guestMagicLink: GuestMagicLink, guest: Guest) = Unit
}