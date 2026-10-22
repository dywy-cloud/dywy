package cloud.dywy.api.guest.request

object GuestMagicLinkRequestFixtures {
    val groupModeJson = rawJson(mode = "GROUP")
    val withoutModeJson = rawJson()
    val unknownModeJson = rawJson(mode = "EVERYONE")

    fun rawJson(mode: String? = null): String =
        if (mode == null) "{}" else """{"mode":"$mode"}"""
}


