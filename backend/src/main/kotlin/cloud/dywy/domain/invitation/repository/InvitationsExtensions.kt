package cloud.dywy.domain.invitation.repository

import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.InvitationListCriteria

private const val INVITATION_BATCH_SIZE = 100

fun Invitations.batches(label: String? = null): Sequence<List<Invitation>> =
    generateSequence(list(InvitationListCriteria(page = 0, size = INVITATION_BATCH_SIZE, label = label))) { page ->
        if (page.page + 1 < page.totalPages) {
            list(InvitationListCriteria(page = page.page + 1, size = INVITATION_BATCH_SIZE, label = label))
        } else {
            null
        }
    }.map { it.items }

fun Invitations.listAll(label: String? = null): List<Invitation> = batches(label).flatten().toList()
