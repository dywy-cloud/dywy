package cloud.dywy.infrastructure.guest.repository

import cloud.dywy.domain.guest.entity.ConsumedGuestMagicLinkToken
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.guest.entity.GuestMagicLink
import cloud.dywy.domain.guest.entity.GuestMagicLinkAccessToken
import cloud.dywy.domain.guest.repository.GuestMagicLinkTokens
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.shared.Dates.nowUtcMillis
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Repository
@OptIn(ExperimentalUuidApi::class)
class GuestMagicLinkTokenExposedRepository : GuestMagicLinkTokens {

    @Transactional
    override fun create(guestMagicLink: GuestMagicLink) {
        val now = nowUtcMillis()
        lockGuest(guestMagicLink.guestId)
        invalidateActiveTokens(guestMagicLink.guestId, now)
        insertActiveToken(guestMagicLink, now)
    }

    @Transactional
    override fun consumeIfValid(token: GuestMagicLinkAccessToken, usedAt: LocalDateTime): ConsumedGuestMagicLinkToken? {
        val existing = GuestMagicLinkTokenTable.selectAll()
            .where {
                (GuestMagicLinkTokenTable.tokenHash eq token.hashed()) and
                    GuestMagicLinkTokenTable.usedAt.isNull() and
                    GuestMagicLinkTokenTable.expiresAt.greater(usedAt)
            }
            .firstOrNull()
            ?: return null

        val updatedRows = GuestMagicLinkTokenTable.update({
            (GuestMagicLinkTokenTable.id eq existing[GuestMagicLinkTokenTable.id]) and
                GuestMagicLinkTokenTable.usedAt.isNull()
        }) {
            it[GuestMagicLinkTokenTable.usedAt] = usedAt
        }

        return if (updatedRows == 1) existing.toConsumedToken() else null
    }

    private fun lockGuest(guestId: GuestId) {
        GuestTable.selectAll()
            .where { GuestTable.id eq guestId.value }
            .forUpdate()
            .firstOrNull()
    }

    private fun invalidateActiveTokens(guestId: GuestId, now: LocalDateTime) {
        GuestMagicLinkTokenTable.update({
            (GuestMagicLinkTokenTable.guestId eq guestId.value) and
                    GuestMagicLinkTokenTable.usedAt.isNull()
        }) { it[usedAt] = now }
    }

    private fun insertActiveToken(guestMagicLink: GuestMagicLink, now: LocalDateTime) {
        GuestMagicLinkTokenTable.insert {
            it[id] = Uuid.generateV7()
            it[GuestMagicLinkTokenTable.tokenHash] = guestMagicLink.token.hashed()
            it[GuestMagicLinkTokenTable.invitationId] = guestMagicLink.invitationId.value
            it[GuestMagicLinkTokenTable.guestId] = guestMagicLink.guestId.value
            it[creationDate] = now
            it[GuestMagicLinkTokenTable.expiresAt] = guestMagicLink.expiresAt
            it[usedAt] = null
        }
    }

    private fun ResultRow.toConsumedToken() = ConsumedGuestMagicLinkToken(
        invitationId = InvitationId(this[GuestMagicLinkTokenTable.invitationId]),
        guestId = GuestId(this[GuestMagicLinkTokenTable.guestId]),
    )
}
