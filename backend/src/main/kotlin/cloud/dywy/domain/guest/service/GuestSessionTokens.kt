package cloud.dywy.domain.guest.service

import cloud.dywy.domain.guest.entity.GuestSession

interface GuestSessionTokens {

    fun issue(session: GuestSession): String

    fun verify(token: String): GuestSession?
}

