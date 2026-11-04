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
import cloud.dywy.domain.rsvp.entity.RsvpStatus
import cloud.dywy.infrastructure.config.GuestProperties
import cloud.dywy.infrastructure.guest.repository.GuestTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import java.sql.Connection
import java.sql.ResultSet
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import kotlin.uuid.Uuid

@Repository
class InvitationsExposedRepository(
    private val guestProperties: GuestProperties,
) : Invitations {

    private val listOrder = arrayOf(
        InvitationTable.updateDate to SortOrder.DESC,
        InvitationTable.id to SortOrder.ASC,
    )

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
        val totalItems = filteredByLabel(criteria.label).count()
        val totalPages = if (totalItems == 0L) 0 else ((totalItems - 1) / criteria.size + 1).toInt()
        val offset = criteria.page.toLong() * criteria.size

        val items = filteredByLabel(criteria.label)
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
    override fun listByRsvpStatus(criteria: InvitationListCriteria, rsvpStatus: RsvpStatus): InvitationPage {
        val conn = TransactionManager.current().connection.connection as Connection
        val labelFilter = if (criteria.label.isNullOrBlank()) "" else "and i.invitation_data ->> 'label' ilike '%' || ? || '%'"
        val offset = criteria.page.toLong() * criteria.size
        val escapedLabel = criteria.label.escapeLike()

        val baseCte = """
            with candidate as (
                select i.id, i.update_date,
                       count(g.guest_id) as total,
                       count(*) filter (where r.attendance = 'ATTENDING') as attending,
                       count(*) filter (where r.attendance = 'DECLINED') as declined
                from invitation i
                cross join lateral jsonb_array_elements_text(i.invitation_data -> 'guestIds') as g(guest_id)
                left join guest_rsvp r on r.guest_id = g.guest_id::uuid
                where 1=1 $labelFilter
                group by i.id, i.update_date
            ),
            with_status as (
                select c.id, c.update_date,
                       case
                           when c.attending = 0 and c.declined = 0 then 'PENDING'
                           when c.attending + c.declined < c.total then 'PARTIALLY_ANSWERED'
                           when c.declined = 0 then 'ACCEPTED_ALL'
                           when c.attending = 0 then 'DECLINED_ALL'
                           else 'PARTIALLY_ACCEPTED'
                       end as status
                from candidate c
            )
        """.trimIndent()

        val countSql = "$baseCte select count(*) from with_status where status = ?"
        val totalItems = conn.queryLongStatus(countSql, escapedLabel, rsvpStatus.name)
        val totalPages = totalItems.toTotalPages(criteria.size)

        if (totalItems == 0L) {
            return emptyInvitationPage(criteria, totalItems, totalPages)
        }

        val dataCte = """
            $baseCte,
            paginated as (
                select id from with_status
                where status = ?
                order by update_date desc, id asc
                limit ? offset ?
            )
        """.trimIndent()

        val dataSql = "$dataCte select i.id, i.version, i.creation_date, i.update_date, i.invitation_data from paginated p join invitation i on i.id = p.id order by i.update_date desc, i.id asc"
        val items = conn.queryInvitations(dataSql, escapedLabel, rsvpStatus.name, criteria.size, offset)

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

    private fun filteredByLabel(label: String?) =
        InvitationTable.selectAll().let { query ->
            if (label == null) query else query.where { labelContains(label) }
        }

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

    private fun String?.escapeLike() = if (isNullOrBlank()) null else replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    private fun Long.toTotalPages(size: Int) = if (this == 0L) 0 else ((this - 1) / size + 1).toInt()

    private fun Connection.queryLongStatus(sql: String, escapedLabel: String?, status: String): Long =
        prepareStatement(sql).use { ps ->
            var index = 1
            if (escapedLabel != null) ps.setString(index++, escapedLabel)
            ps.setString(index, status)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getLong(1) else 0L }
        }

    private fun Connection.queryInvitations(sql: String, escapedLabel: String?, status: String, size: Int, offset: Long): List<Invitation> =
        prepareStatement(sql).use { ps ->
            var index = 1
            if (escapedLabel != null) ps.setString(index++, escapedLabel)
            ps.setString(index++, status)
            ps.setInt(index++, size)
            ps.setLong(index, offset)
            ps.executeQuery().use { rs -> rs.toInvitationList() }
        }

    private fun ResultSet.toInvitationList(): List<Invitation> {
        val rows = mutableListOf<RawInvitationRow>()
        while (next()) {
            rows.add(RawInvitationRow(
                id = Uuid.parse(getString("id")),
                version = getLong("version"),
                creationDate = getTimestamp("creation_date").toLocalDateTime(),
                updateDate = getTimestamp("update_date").toLocalDateTime(),
                invitationDataJson = getString("invitation_data"),
            ))
        }
        if (rows.isEmpty()) return emptyList()

        val guestIds = rows.flatMap { it.data.parsedGuestIds(InvitationId(it.id)) }.toSet()
        val guestsById = fetchGuestsByIds(guestIds)
        return rows.map { row ->
            row.data.toInvitation(
                id = InvitationId(row.id),
                version = row.version,
                creationDate = row.creationDate,
                updateDate = row.updateDate,
                guestsById = guestsById,
            )
        }
    }

    private data class RawInvitationRow(
        val id: Uuid,
        val version: Long,
        val creationDate: java.time.LocalDateTime,
        val updateDate: java.time.LocalDateTime,
        val invitationDataJson: String,
    ) {
        val data: InvitationData = parseInvitationData(InvitationId(id), invitationDataJson)
    }

    private fun emptyInvitationPage(
        criteria: InvitationListCriteria,
        totalItems: Long,
        totalPages: Int,
    ) = InvitationPage(
        items = emptyList(),
        page = criteria.page,
        size = criteria.size,
        totalItems = totalItems,
        totalPages = totalPages,
    )

    private fun ResultRow.toInvitation(data: InvitationData, guestsById: Map<Uuid, Guest>) =
        data.toInvitation(
            id = InvitationId(this[InvitationTable.id]),
            version = this[InvitationTable.version],
            creationDate = this[InvitationTable.creationDate],
            updateDate = this[InvitationTable.updateDate],
            guestsById = guestsById,
        )
}

