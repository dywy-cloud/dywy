package cloud.dywy.domain.guest.service

import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.GuestMagicLink

interface GuestMagicLinkSender {

    fun send(guestMagicLink: GuestMagicLink, guest: Guest)
}