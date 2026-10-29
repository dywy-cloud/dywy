package cloud.dywy.application.invitation

import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.InvitationListCriteria
import cloud.dywy.domain.invitation.entity.InvitationPage
import cloud.dywy.domain.invitation.entity.InvitationSearch
import cloud.dywy.domain.invitation.repository.Invitations
import cloud.dywy.domain.invitation.repository.batches
import cloud.dywy.domain.rsvp.entity.RsvpStatus
import cloud.dywy.domain.rsvp.entity.RsvpSummary
import org.springframework.stereotype.Service

@Service
class InvitationLister(
    private val invitations: Invitations,
    private val invitationRsvpLoader: InvitationRsvpLoader,
) {

    fun list(search: InvitationSearch): InvitationPage =
        search.rsvpStatus?.let { listByRsvpStatus(search.criteria, it) } ?: invitations.list(search.criteria)

    private fun listByRsvpStatus(criteria: InvitationListCriteria, rsvpStatus: RsvpStatus): InvitationPage =
        (criteria.page.toLong() * criteria.size).let { windowStart ->
            invitations.batches(criteria.label)
                .flatMap { batch ->
                    invitationRsvpLoader.load(batch).let { rsvps ->
                        batch.filter { invitation ->
                            RsvpSummary.of(invitation.guestIds, invitation.guestIds.mapNotNull(rsvps::get)).status == rsvpStatus
                        }
                    }
                }
                .fold(0L to emptyList<Invitation>()) { (matchingCount, pageItems), invitation ->
                    (matchingCount + 1) to
                        if (matchingCount in windowStart until windowStart + criteria.size) pageItems + invitation else pageItems
                }
                .let { (totalItems, pageItems) ->
                    InvitationPage(
                        items = pageItems,
                        page = criteria.page,
                        size = criteria.size,
                        totalItems = totalItems,
                        totalPages = ((totalItems + criteria.size - 1) / criteria.size).toInt(),
                    )
                }
        }
}
