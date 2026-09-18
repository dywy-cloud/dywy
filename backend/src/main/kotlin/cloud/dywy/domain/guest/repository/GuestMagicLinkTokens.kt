package cloud.dywy.domain.guest.repository

import cloud.dywy.domain.guest.entity.ConsumedGuestMagicLinkToken
import cloud.dywy.domain.guest.entity.GuestMagicLink
import cloud.dywy.domain.guest.entity.GuestMagicLinkAccessToken
import java.time.LocalDateTime

interface GuestMagicLinkTokens {

    fun create(guestMagicLink: GuestMagicLink)

    fun consumeIfValid(token: GuestMagicLinkAccessToken, usedAt: LocalDateTime): ConsumedGuestMagicLinkToken?
}

