package io.medatarun.model.infra.db.migrations

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.DbMigrationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import java.sql.Connection
import java.sql.PreparedStatement

/**
 * Simple best-effort patch for legacy events:
 * - queue all `type_created` without `typeId`
 * - track assigned type ids
 * - replay events in order and assign unknown referenced ids to next pending create
 * - generate ids for remaining pending creates
 */
class V002_ModelFixTypeEvents {
    private class EventRow(
        val id: ByteArray,
        val eventType: String,
        val payload: String
    )

    private class PendingCreateType(
        val id: ByteArray,
        val payload: String
    )

    fun migrate(ctx: DbMigrationContext) {
        ctx.withConnection { connection ->
            val events = readEvents(connection)
            val pendingCreates = ArrayDeque<PendingCreateType>()
            val assignedTypeIds = linkedSetOf<String>()
            val typeCreatedEvents = events.filter { event -> event.eventType == EVENT_TYPE_TYPE_CREATED }
            val nonTypeCreatedEvents = events.filter { event -> event.eventType != EVENT_TYPE_TYPE_CREATED }

            for (event in typeCreatedEvents) {
                pendingCreates.addLast(PendingCreateType(event.id, event.payload))
            }

            connection.prepareStatement(
                """
                UPDATE model_event
                SET payload = ?
                WHERE id = ?
                """.trimIndent()
            ).use { updateStatement ->
                for (event in nonTypeCreatedEvents) {
                    val payloadObject = parsePayloadObject(event.payload)
                    val referencedTypeId = readTypeIdReference(payloadObject)
                    if (referencedTypeId != null && !assignedTypeIds.contains(referencedTypeId)) {
                        val pending = if (pendingCreates.isEmpty()) null else pendingCreates.removeFirst()
                        if (pending != null) {
                            patchPendingTypeCreated(updateStatement, pending, referencedTypeId)
                        }
                        assignedTypeIds.add(referencedTypeId)
                    }
                }

                while (!pendingCreates.isEmpty()) {
                    val pending = pendingCreates.removeFirst()
                    val generatedTypeId = UuidUtils.generateV7().toString()
                    assignedTypeIds.add(generatedTypeId)
                    patchPendingTypeCreated(updateStatement, pending, generatedTypeId)
                }
            }
        }
    }

    private fun readTypeIdReference(payloadObject: JsonObject): String? {
        val directTypeId = readStringValue(payloadObject, JSON_KEY_TYPE_ID)
        if (directTypeId != null) return directTypeId
        return readStringValue(payloadObject, JSON_KEY_IDENTITY_ATTRIBUTE_TYPE_ID)
    }

    private fun parsePayloadObject(payload: String): JsonObject {
        return Json.parseToJsonElement(payload).jsonObject
    }

    private fun readStringValue(payloadObject: JsonObject, key: String): String? {
        val value = payloadObject[key] ?: return null
        if (value !is JsonPrimitive) return null
        return value.content
    }

    private fun readEvents(connection: Connection): List<EventRow> {
        val events = mutableListOf<EventRow>()
        connection.prepareStatement(
            """
            SELECT id, event_type, payload
            FROM model_event
            ORDER BY stream_revision ASC
            """.trimIndent()
        ).use { select ->
            select.executeQuery().use { rs ->
                while (rs.next()) {
                    val eventId = requireNotNull(rs.getBytes("id"))
                    val eventType = requireNotNull(rs.getString("event_type"))
                    val payload = requireNotNull(rs.getString("payload"))
                    events.add(EventRow(eventId, eventType, payload))
                }
            }
        }
        return events
    }

    private fun patchPendingTypeCreated(
        updateStatement: PreparedStatement,
        pending: PendingCreateType,
        typeId: String
    ) {
        val payloadObject = parsePayloadObject(pending.payload)
        val patched = buildJsonObject {
            for (entry in payloadObject.entries) {
                put(entry.key, entry.value)
            }
            put(JSON_KEY_TYPE_ID, JsonPrimitive(typeId))
        }
        val patchedPayload = patched.toString()
        updateStatement.setString(1, patchedPayload)
        updateStatement.setBytes(2, pending.id)
        updateStatement.executeUpdate()
    }

    companion object {
        private const val EVENT_TYPE_TYPE_CREATED = "type_created"
        private const val JSON_KEY_TYPE_ID = "typeId"
        private const val JSON_KEY_IDENTITY_ATTRIBUTE_TYPE_ID = "identityAttributeTypeId"
    }
}