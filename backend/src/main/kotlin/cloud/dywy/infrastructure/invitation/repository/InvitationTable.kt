package cloud.dywy.infrastructure.invitation.repository

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.QueryBuilder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.TextColumnType
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.json.jsonb

object InvitationTable : Table("invitation") {
    val id = uuid("id")
    val version = long("version")
    val creationDate = datetime("creation_date")
    val updateDate = datetime("update_date")
    val invitationData = jsonb(
        "invitation_data",
        { value: String -> value },
        { value: String -> value },
    )
    override val primaryKey = PrimaryKey(id)
}

internal fun accessTokenEquals(token: String): Op<Boolean> = object : Op<Boolean>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append(InvitationTable.invitationData)
        queryBuilder.append(" ->> 'accessToken' = ")
        queryBuilder.registerArgument(TextColumnType(), token)
    }
}

internal fun guestIdsOverlap(guestIds: Collection<String>): Op<Boolean> = object : Op<Boolean>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("jsonb_exists_any(")
        queryBuilder.append(InvitationTable.invitationData)
        queryBuilder.append(" -> 'guestIds', ARRAY[")
        guestIds.forEachIndexed { index, guestId ->
            if (index > 0) queryBuilder.append(", ")
            queryBuilder.registerArgument(TextColumnType(), guestId)
        }
        queryBuilder.append("]::text[])")
    }
}

internal fun guestIsNotAssigned(guestId: Expression<*>): Op<Boolean> = object : Op<Boolean>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("NOT EXISTS (SELECT 1 FROM ")
        queryBuilder.append(InvitationTable.nameInDatabaseCase())
        queryBuilder.append(" WHERE jsonb_exists(")
        queryBuilder.append(InvitationTable.invitationData)
        queryBuilder.append(" -> 'guestIds', CAST(")
        queryBuilder.append(guestId)
        queryBuilder.append(" AS TEXT)))")
    }
}
