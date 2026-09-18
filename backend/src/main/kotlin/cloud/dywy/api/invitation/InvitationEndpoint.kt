package cloud.dywy.api.invitation

import cloud.dywy.api.common.bindOrBadRequest
import cloud.dywy.api.common.intQueryParam
import cloud.dywy.api.common.invitationIdPathParam
import cloud.dywy.api.invitation.request.AddInvitationRequest
import cloud.dywy.api.invitation.request.UpdateInvitationRequest
import cloud.dywy.api.invitation.response.AlreadyAssignedInvitationGuestsResponse
import cloud.dywy.api.invitation.response.InvalidInvitationGuestsResponse
import cloud.dywy.api.invitation.response.MissingInvitationGuestsResponse
import cloud.dywy.api.invitation.response.toResponse
import cloud.dywy.application.invitation.InvitationAdder
import cloud.dywy.application.invitation.InvitationGetter
import cloud.dywy.application.invitation.InvitationLister
import cloud.dywy.application.invitation.InvitationUpdater
import cloud.dywy.application.invitation.result.AddInvitationResult
import cloud.dywy.application.invitation.result.UpdateInvitationResult
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.InvitationListCriteria
import jakarta.validation.Validator
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class InvitationEndpoint(
    private val invitationAdder: InvitationAdder,
    private val invitationLister: InvitationLister,
    private val invitationGetter: InvitationGetter,
    private val invitationUpdater: InvitationUpdater,
    private val validator: Validator,
) {
    fun listInvitations(request: ServerRequest): ServerResponse =
        request.intQueryParam("page", default = 0)?.takeIf { it >= 0 }?.let { page ->
            request.intQueryParam("size", default = 20)?.takeIf { it > 0 }?.let { size ->
                ServerResponse.ok()
                    .body(invitationLister.list(InvitationListCriteria(page = page, size = size)).toResponse())
            }
        } ?: ServerResponse.badRequest().build()

    fun addInvitation(request: ServerRequest): ServerResponse =
        request.bindOrBadRequest<AddInvitationRequest>(validator) { payload ->
            payload.toCommandOrNull()
                ?.let { command ->
                    when (val result = invitationAdder.add(command)) {
                        is AddInvitationResult.Added -> ServerResponse.status(HttpStatus.CREATED)
                            .body(result.invitation.toResponse())

                        is AddInvitationResult.MissingGuests -> ServerResponse.badRequest().body(
                            MissingInvitationGuestsResponse(message = "At least one guest is required.")
                        )

                        is AddInvitationResult.InvalidGuests -> ServerResponse.badRequest().body(
                            InvalidInvitationGuestsResponse(
                                message = "Some guests were not found or are archived.",
                                guestIds = result.guestIds.map(GuestId::toString).sorted(),
                            )
                        )

                        is AddInvitationResult.AlreadyAssignedGuests -> ServerResponse.status(HttpStatus.CONFLICT).body(
                            AlreadyAssignedInvitationGuestsResponse(
                                message = "Some guests are already assigned to another invitation.",
                                guestIds = result.guestIds.map(GuestId::toString).sorted(),
                            )
                        )
                    }
                } ?: ServerResponse.badRequest().build()
        }

    fun getInvitation(request: ServerRequest): ServerResponse =
        request.invitationIdPathParam()?.let { id ->
            invitationGetter.get(id)
                ?.toResponse()
                ?.let(ServerResponse.ok()::body)
                ?: ServerResponse.notFound().build()
        } ?: ServerResponse.badRequest().build()

    fun updateInvitation(request: ServerRequest): ServerResponse =
        request.invitationIdPathParam()?.let { id ->
            request.bindOrBadRequest<UpdateInvitationRequest>(validator) { payload ->
                payload.toCommandOrNull(id)
                    ?.let { command ->
                        when (val result = invitationUpdater.update(command)) {
                            is UpdateInvitationResult.Updated -> ServerResponse.ok().body(result.invitation.toResponse())
                            is UpdateInvitationResult.VersionConflict -> ServerResponse.status(HttpStatus.CONFLICT)
                                .body(mapOf("message" to "This invitation has been modified elsewhere. Please reload and try again."))
                            is UpdateInvitationResult.NotFound -> ServerResponse.notFound().build()
                            is UpdateInvitationResult.MissingGuests -> ServerResponse.badRequest().body(
                                MissingInvitationGuestsResponse(message = "At least one guest is required.")
                            )

                            is UpdateInvitationResult.InvalidGuests -> ServerResponse.badRequest().body(
                                InvalidInvitationGuestsResponse(
                                    message = "Some guests were not found or are archived.",
                                    guestIds = result.guestIds.map(GuestId::toString).sorted(),
                                )
                            )

                            is UpdateInvitationResult.AlreadyAssignedGuests -> ServerResponse.status(HttpStatus.CONFLICT)
                                .body(
                                    AlreadyAssignedInvitationGuestsResponse(
                                        message = "Some guests are already assigned to another invitation.",
                                        guestIds = result.guestIds.map(GuestId::toString).sorted(),
                                    )
                                )
                        }
                    } ?: ServerResponse.badRequest().build()
            }
        } ?: ServerResponse.badRequest().build()
}
