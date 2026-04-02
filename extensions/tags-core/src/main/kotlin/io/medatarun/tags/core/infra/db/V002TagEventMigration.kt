package io.medatarun.tags.core.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.DbDialect
import io.medatarun.platform.db.DbMigrationContext
import io.medatarun.platform.db.jdbc.getUuidFromString
import io.medatarun.platform.db.jdbc.setUUID
import io.medatarun.security.AppActorId
import io.medatarun.type.commons.instant.InstantAdapters
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

/**
 * Backfills v002 `tag_event` and history tables from legacy v001 snapshot tables.
 */
internal class V002TagEventMigration(private val maintenanceActorId: AppActorId) {
    private class V002TagEventMigrationException(message: String) : MedatarunException(message)

    fun migrate(ctx: DbMigrationContext) {
        if (ctx.dialect != DbDialect.SQLITE) {
            throw V002TagEventMigrationException("tags-core v002 backfill supports only sqlite, dialect=${ctx.dialect}.")
        }
        val migrationInstant = Instant.now()
        val streamRevisionTracker = StreamRevisionTracker()
        ctx.withConnection { connection ->
            connection.prepareStatement(INSERT_TAG_EVENT_SQL).use { eventStatement ->
                connection.prepareStatement(INSERT_TAG_HISTORY_SQL).use { tagHistoryStatement ->
                    connection.prepareStatement(INSERT_TAG_GROUP_HISTORY_SQL).use { groupHistoryStatement ->
                        migrateTagGroups(connection, eventStatement, groupHistoryStatement, streamRevisionTracker, migrationInstant)
                        migrateTags(connection, eventStatement, tagHistoryStatement, streamRevisionTracker, migrationInstant)
                    }
                }
            }
        }
    }

    private fun migrateTagGroups(
        connection: Connection,
        eventStatement: PreparedStatement,
        historyStatement: PreparedStatement,
        streamRevisionTracker: StreamRevisionTracker,
        migrationInstant: Instant
    ) {
        connection.prepareStatement(SELECT_TAG_GROUPS_SQL).use { selectStatement ->
            selectStatement.executeQuery().use { rs ->
                while (rs.next()) {
                    val groupId = rs.getUuidFromString("id")
                        ?: throw V002TagEventMigrationException("tag_group.id must not be null.")
                    val eventId = UuidUtils.generateV7()
                    insertTagEvent(
                        eventStatement,
                        eventId,
                        GLOBAL_SCOPE_TYPE,
                        null,
                        streamRevisionTracker.next(GLOBAL_SCOPE_TYPE, null),
                        EVENT_TYPE_TAG_GROUP_CREATED,
                        tagGroupCreatedPayload(rs, groupId.toString()),
                        migrationInstant
                    )
                    historyStatement.setUUID(1, UuidUtils.generateV7())
                    historyStatement.setUUID(2, eventId)
                    historyStatement.setUUID(3, groupId)
                    historyStatement.setString(4, rs.getString("key"))
                    historyStatement.setString(5, rs.getString("name"))
                    historyStatement.setString(6, rs.getString("description"))
                    historyStatement.setString(7, InstantAdapters.toSqlTimestampString(migrationInstant))
                    historyStatement.setNull(8, java.sql.Types.VARCHAR)
                    historyStatement.executeUpdate()
                }
            }
        }
    }

    private fun migrateTags(
        connection: Connection,
        eventStatement: PreparedStatement,
        historyStatement: PreparedStatement,
        streamRevisionTracker: StreamRevisionTracker,
        migrationInstant: Instant
    ) {
        connection.prepareStatement(SELECT_TAGS_SQL).use { selectStatement ->
            selectStatement.executeQuery().use { rs ->
                while (rs.next()) {
                    val scopeType = rs.getString("scope_type")
                    val scopeId = normalizedScopeId(scopeType, rs)
                    val tagId = rs.getUuidFromString("id")
                        ?: throw V002TagEventMigrationException("tag.id must not be null.")
                    val groupIdRaw = rs.getString("tag_group_id")
                    val groupId = if (groupIdRaw == null) {
                        null
                    } else {
                        rs.getUuidFromString("tag_group_id")
                            ?: throw V002TagEventMigrationException("tag.tag_group_id must not be null.")
                    }
                    val eventId = UuidUtils.generateV7()
                    insertTagEvent(
                        eventStatement,
                        eventId,
                        scopeType,
                        scopeId,
                        streamRevisionTracker.next(scopeType, scopeId),
                        EVENT_TYPE_TAG_CREATED,
                        tagCreatedPayload(rs, tagId.toString(), scopeType, scopeId?.toString(), groupId?.toString()),
                        migrationInstant
                    )
                    historyStatement.setUUID(1, UuidUtils.generateV7())
                    historyStatement.setUUID(2, eventId)
                    historyStatement.setUUID(3, tagId)
                    historyStatement.setString(4, scopeType)
                    historyStatement.setUUID(5, scopeId)
                    historyStatement.setUUID(6, groupId)
                    historyStatement.setString(7, rs.getString("key"))
                    historyStatement.setString(8, rs.getString("name"))
                    historyStatement.setString(9, rs.getString("description"))
                    historyStatement.setString(10, InstantAdapters.toSqlTimestampString(migrationInstant))
                    historyStatement.setNull(11, java.sql.Types.VARCHAR)
                    historyStatement.executeUpdate()
                }
            }
        }
    }


    private fun normalizedScopeId(scopeType: String, rs: ResultSet): UUID? {
        val scopeIdRaw = rs.getString("scope_id")
        if (scopeType == GLOBAL_SCOPE_TYPE) {
            if (scopeIdRaw != null) throw V002TagEventMigrationException("global tag row has non-null scope_id.")
            return null
        }
        if (scopeIdRaw == null) throw V002TagEventMigrationException("local tag row has null scope_id for scope_type=$scopeType.")
        return rs.getUuidFromString("scope_id")
            ?: throw V002TagEventMigrationException("tag.scope_id must not be null.")
    }

    private fun insertTagEvent(
        statement: PreparedStatement,
        eventId: UUID,
        scopeType: String,
        scopeId: UUID?,
        streamRevision: Int,
        eventType: String,
        payload: String,
        createdAt: Instant
    ) {
        statement.setUUID(1, eventId)
        statement.setString(2, scopeType)
        statement.setUUID(3, scopeId)
        statement.setInt(4, streamRevision)
        statement.setString(5, eventType)
        statement.setInt(6, EVENT_VERSION_1)
        statement.setUUID(7, maintenanceActorId.value)
        statement.setString(8, TRACEABILITY_ORIGIN)
        statement.setString(9, InstantAdapters.toSqlTimestampString(createdAt))
        statement.setString(10, payload)
        statement.executeUpdate()
    }

    private fun tagGroupCreatedPayload(rs: ResultSet, groupId: String): String {
        return buildJsonObject {
            put("tagGroupId", groupId)
            put("key", rs.getString("key"))
            put("name", rs.getString("name"))
            put("description", rs.getString("description"))
        }.toString()
    }

    private fun tagCreatedPayload(
        rs: ResultSet,
        tagId: String,
        scopeType: String,
        scopeId: String?,
        groupId: String?
    ): String {
        return buildJsonObject {
            put("tagId", tagId)
            put("scope", buildJsonObject {
                put("type", scopeType)
                if (scopeId != null) {
                    put("id", scopeId)
                }
            })
            put("groupId", groupId)
            put("key", rs.getString("key"))
            put("name", rs.getString("name"))
            put("description", rs.getString("description"))
        }.toString()
    }

    /**
     * Tracks event stream revisions by `(scope_type, scope_id)` key.
     */
    private class StreamRevisionTracker {
        private val revisionsByStream = mutableMapOf<String, Int>()

        fun next(scopeType: String, scopeId: UUID?): Int {
            val streamKey = streamKey(scopeType, scopeId)
            val currentRevision = revisionsByStream[streamKey]
            val nextRevision = if (currentRevision == null) 1 else currentRevision + 1
            revisionsByStream[streamKey] = nextRevision
            return nextRevision
        }

        private fun streamKey(scopeType: String, scopeId: UUID?): String {
            return scopeType + "::" + (scopeId?.toString() ?: "_null_")
        }
    }

    companion object {
        private const val GLOBAL_SCOPE_TYPE = "global"
        private const val EVENT_TYPE_TAG_GROUP_CREATED = "tag_group_created"
        private const val EVENT_TYPE_TAG_CREATED = "tag_created"
        private const val EVENT_VERSION_1 = 1
        private const val TRACEABILITY_ORIGIN = "migration:tags:v002"
        private const val SELECT_TAG_GROUPS_SQL = """
SELECT id, key, name, description
FROM tag_group
ORDER BY key, id
"""

        private const val SELECT_TAGS_SQL = """
SELECT id, scope_type, scope_id, tag_group_id, key, name, description
FROM tag
ORDER BY CASE WHEN scope_type = 'global' AND scope_id IS NULL THEN 0 ELSE 1 END, scope_type, scope_id, key, id
"""

        private const val INSERT_TAG_EVENT_SQL = """
INSERT INTO tag_event
(id, scope_type, scope_id, stream_revision, event_type, event_version, actor_id, traceability_origin, created_at, payload)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
"""

        private const val INSERT_TAG_HISTORY_SQL = """
INSERT INTO tag_view_history_tag
(id, tag_event_id, tag_id, scope_type, scope_id, tag_group_id, key, name, description, valid_from, valid_to)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
"""

        private const val INSERT_TAG_GROUP_HISTORY_SQL = """
INSERT INTO tag_view_history_tag_group
(id, tag_event_id, tag_group_id, key, name, description, valid_from, valid_to)
VALUES (?, ?, ?, ?, ?, ?, ?, ?)
"""
    }
}
