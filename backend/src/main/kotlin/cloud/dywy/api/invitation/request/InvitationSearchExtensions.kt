package cloud.dywy.api.invitation.request

import cloud.dywy.api.common.intQueryParam
import cloud.dywy.api.common.rsvpStatusQueryParam
import cloud.dywy.domain.invitation.entity.InvitationListCriteria
import cloud.dywy.domain.invitation.entity.InvitationSearch
import org.springframework.web.servlet.function.ServerRequest

internal fun ServerRequest.invitationSearchOrNull(): InvitationSearch? =
    runCatching {
        InvitationSearch(
            criteria = InvitationListCriteria(
                page = requireNotNull(intQueryParam("page", default = 0)),
                size = requireNotNull(intQueryParam("size", default = InvitationListCriteria.DEFAULT_SIZE)),
                label = param("label").orElse(null)?.trim()?.takeIf { it.isNotEmpty() },
            ),
            rsvpStatus = rsvpStatusQueryParam().getOrThrow(),
        )
    }.getOrNull()
