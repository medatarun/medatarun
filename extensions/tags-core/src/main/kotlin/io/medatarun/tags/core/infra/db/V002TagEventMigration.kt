package io.medatarun.tags.core.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.DbDialect
import io.medatarun.platform.db.DbMigrationContext
import io.medatarun.security.AppActorId
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.ByteBuffer
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
                    val groupId = parseUuidString(rs.getString("id"), "tag_group.id")
                    val groupIdBytes = toBytes(groupId)
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
                    historyStatement.setBytes(1, toBytes(UuidUtils.generateV7()))
                    historyStatement.setBytes(2, toBytes(eventId))
                    historyStatement.setBytes(3, groupIdBytes)
                    historyStatement.setString(4, rs.getString("key"))
                    historyStatement.setString(5, rs.getString("name"))
                    historyStatement.setString(6, rs.getString("description"))
                    historyStatement.setString(7, migrationInstant.toString())
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
                    val rawScopeId = rs.getString("scope_id")
                    val scopeId = normalizedScopeId(scopeType, rawScopeId)
                    val scopeIdBytes = if (scopeId == null) null else toBytes(scopeId)
                    val tagId = parseUuidString(rs.getString("id"), "tag.id")
                    val tagIdBytes = toBytes(tagId)
                    val groupIdRaw = rs.getString("tag_group_id")
                    val groupId = if (groupIdRaw == null) null else parseUuidString(groupIdRaw, "tag.tag_group_id")
                    val groupIdBytes = if (groupId == null) null else toBytes(groupId)
                    val eventId = UuidUtils.generateV7()
                    insertTagEvent(
                        eventStatement,
                        eventId,
                        scopeType,
                        scopeIdBytes,
                        streamRevisionTracker.next(scopeType, scopeId),
                        EVENT_TYPE_TAG_CREATED,
                        tagCreatedPayload(rs, tagId.toString(), scopeType, scopeId?.toString(), groupId?.toString()),
                        migrationInstant
                    )
                    historyStatement.setBytes(1, toBytes(UuidUtils.generateV7()))
                    historyStatement.setBytes(2, toBytes(eventId))
                    historyStatement.setBytes(3, tagIdBytes)
                    historyStatement.setString(4, scopeType)
                    if (scopeIdBytes == null) historyStatement.setNull(5, java.sql.Types.BLOB) else historyStatement.setBytes(5, scopeIdBytes)
                    if (groupIdBytes == null) historyStatement.setNull(6, java.sql.Types.BLOB) else historyStatement.setBytes(6, groupIdBytes)
                    historyStatement.setString(7, rs.getString("key"))
                    historyStatement.setString(8, rs.getString("name"))
                    historyStatement.setString(9, rs.getString("description"))
                    historyStatement.setString(10, migrationInstant.toString())
                    historyStatement.setNull(11, java.sql.Types.VARCHAR)
                    historyStatement.executeUpdate()
                }
            }
        }
    }

    private fun normalizedScopeId(scopeType: String, scopeIdRaw: String?): UUID? {
        if (scopeType == GLOBAL_SCOPE_TYPE) {
            if (scopeIdRaw != null) throw V002TagEventMigrationException("global tag row has non-null scope_id.")
            return null
        }
        if (scopeIdRaw == null) throw V002TagEventMigrationException("local tag row has null scope_id for scope_type=$scopeType.")
        return parseUuidString(scopeIdRaw, "tag.scope_id")
    }

    private fun insertTagEvent(
        statement: PreparedStatement,
        eventId: UUID,
        scopeType: String,
        scopeIdBytes: ByteArray?,
        streamRevision: Int,
        eventType: String,
        payload: String,
        createdAt: Instant
    ) {
        statement.setBytes(1, toBytes(eventId))
        statement.setString(2, scopeType)
        if (scopeIdBytes == null) statement.setNull(3, java.sql.Types.BLOB) else statement.setBytes(3, scopeIdBytes)
        statement.setInt(4, streamRevision)
        statement.setString(5, eventType)
        statement.setInt(6, EVENT_VERSION_1)
        statement.setBytes(7, toBytes(maintenanceActorId.value))
        statement.setString(8, TRACEABILITY_ORIGIN)
        statement.setString(9, createdAt.toString())
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

    private fun parseUuidString(rawValue: String?, label: String): UUID {
        if (rawValue == null) {
            throw V002TagEventMigrationException("$label must not be null.")
        }
        return try {
            UuidUtils.fromString(rawValue)
        } catch (_: IllegalArgumentException) {
            throw V002TagEventMigrationException("$label must be a valid uuid string, got [$rawValue].")
        }
    }

    private fun toBytes(value: UUID): ByteArray {
        val buffer = ByteBuffer.allocate(UUID_BINARY_SIZE)
        buffer.putLong(value.mostSignificantBits)
        buffer.putLong(value.leastSignificantBits)
        return buffer.array()
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
        private const val UUID_BINARY_SIZE = 16

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
