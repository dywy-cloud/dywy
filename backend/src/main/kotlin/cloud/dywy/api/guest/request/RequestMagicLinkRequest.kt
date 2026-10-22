package cloud.dywy.api.guest.request

import cloud.dywy.domain.guest.entity.GuestMagicLinkMode

data class RequestMagicLinkRequest(
    val mode: GuestMagicLinkMode? = null,
)

