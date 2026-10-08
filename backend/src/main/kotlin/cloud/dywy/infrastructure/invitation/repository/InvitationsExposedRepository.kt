package cloud.dywy.infrastructure.invitation.repository

import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.guest.entity.Language
import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.InvitationAccessToken
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.invitation.entity.InvitationListCriteria
import cloud.dywy.domain.invitation.entity.InvitationPage
import cloud.dywy.domain.invitation.repository.Invitations
import cloud.dywy.infrastructure.config.GuestProperties
import cloud.dywy.infrastructure.guest.repository.GuestTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import kotlin.uuid.Uuid

@Repository
class InvitationsExposedRepository(
    private val guestProperties: GuestProperties,
) : Invitations {

    private val listOrder = arrayOf(InvitationTable.id to SortOrder.ASC)

    @Transactional
    override fun add(invitation: Invitation): Invitation =
        invitation.also {
            InvitationTable.insert {
                it[id] = invitation.id.value
                it[version] = invitation.version
                it[creationDate] = invitation.creationDate
                it[updateDate] = invitation.updateDate
                it[invitationData] = invitation.toData().toJson()
            }
        }

    @Transactional
    override fun update(invitation: Invitation): Invitation? =
        InvitationTable.update({ InvitationTable.id eq invitation.id.value }) {
            it[version] = invitation.version
            it[updateDate] = invitation.updateDate
            it[invitationData] = invitation.toData().toJson()
        }.let { if (it == 0) null else invitation }

    @Transactional(readOnly = true)
    override fun findById(id: InvitationId): Invitation? =
        InvitationTable.selectAll()
            .where { InvitationTable.id eq id.value }
            .toList()
            .toInvitations()
            .firstOrNull()

    @Transactional(readOnly = true)
    override fun list(criteria: InvitationListCriteria): InvitationPage {
        val totalItems = InvitationTable.selectAll().count()
        val totalPages = if (totalItems == 0L) 0 else ((totalItems - 1) / criteria.size + 1).toInt()
        val offset = criteria.page.toLong() * criteria.size

        val items = InvitationTable.selectAll()
            .orderBy(*listOrder)
            .limit(criteria.size)
            .offset(offset)
            .toList()
            .toInvitations()

        return InvitationPage(
            items = items,
            page = criteria.page,
            size = criteria.size,
            totalItems = totalItems,
            totalPages = totalPages,
        )
    }

    @Transactional(readOnly = true)
    override fun findAssignedGuestIds(guestIds: Set<GuestId>): Set<GuestId> =
        if (guestIds.isEmpty()) emptySet()
        else {
            val requested = guestIds.map { it.value.toString() }.toSet()
            InvitationTable.select(InvitationTable.id, InvitationTable.invitationData)
                .where { guestIdsOverlap(requested) }
                .flatMap { row -> row.data().guestIds }
                .filter { it in requested }
                .map { GuestId(Uuid.parse(it)) }
                .toSet()
        }

    @Transactional(readOnly = true)
    override fun findInvitationByAccessToken(token: InvitationAccessToken): Invitation? =
        InvitationTable.selectAll()
            .where { accessTokenEquals(token.value) }
            .toList()
            .toInvitations()
            .firstOrNull()

    private fun List<ResultRow>.toInvitations(): List<Invitation> {
        val rows = map { row -> row to row.data() }
        val guestIds = rows.flatMap { (row, data) -> data.parsedGuestIds(row.invitationId()) }.toSet()
        val guestsById = fetchGuestsByIds(guestIds)
        return rows.map { (row, data) -> row.toInvitation(data, guestsById) }
    }

    private fun ResultRow.invitationId() = InvitationId(this[InvitationTable.id])

    private fun ResultRow.data(): InvitationData =
        parseInvitationData(invitationId(), this[InvitationTable.invitationData])

    private fun fetchGuestsByIds(guestIds: Set<Uuid>) =
        if (guestIds.isEmpty()) {
            emptyMap()
        } else {
            GuestTable
                .selectAll()
                .where { GuestTable.id inList guestIds }
                .toList()
                .associate { row ->
                    row[GuestTable.id] to Guest(
                        id = GuestId(row[GuestTable.id]),
                        version = row[GuestTable.version],
                        creationDate = row[GuestTable.creationDate],
                        updateDate = row[GuestTable.updateDate],
                        deletionDate = row[GuestTable.deletionDate],
                        firstName = row[GuestTable.firstName],
                        lastName = row[GuestTable.lastName],
                        email = row[GuestTable.email],
                        language = Language.fromNullable(row[GuestTable.language], guestProperties.defaultLanguage),
                    )
                }
        }

    private fun ResultRow.toInvitation(data: InvitationData, guestsById: Map<Uuid, Guest>) =
        data.toInvitation(
            id = InvitationId(this[InvitationTable.id]),
            version = this[InvitationTable.version],
            creationDate = this[InvitationTable.creationDate],
            updateDate = this[InvitationTable.updateDate],
            guestsById = guestsById,
        )
}

