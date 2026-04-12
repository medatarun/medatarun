package io.medatarun.model.infra.db.migrations

import io.medatarun.platform.db.DbMigrationContext
import io.medatarun.platform.db.jdbc.getUuid
import io.medatarun.platform.db.jdbc.setUUID
import java.sql.Connection
import java.sql.ResultSet

/**
 * Backfills compatibility primary-key snapshots from legacy entity identifier attributes.
 *
 * During compatibility phase each entity snapshot has exactly one primary key entry
 * whose only participant is the legacy `identifier_attribute_snapshot_id`.
 */
class V003_IdentifierAttributeToPrimaryKeys {

    fun migrate(ctx: DbMigrationContext) {
        ctx.withConnection { connection ->
            connection.prepareStatement(
                "SELECT id, lineage_id, identifier_attribute_snapshot_id FROM model_entity_snapshot ORDER BY id"
            ).use { selectEntitiesStatement ->
                selectEntitiesStatement.executeQuery().use { rsEntities ->
                    convertEntitiesPK(rsEntities, ctx, connection)
                }
            }
        }
    }

    private fun convertEntitiesPK(
        rsEntities: ResultSet,
        ctx: DbMigrationContext,
        connection: Connection
    ) {
        while (rsEntities.next()) {
            val entitySnapshotId = requireNotNull(rsEntities.getUuid("id", ctx.dialect))
            val entityLineageId = requireNotNull(rsEntities.getUuid("lineage_id", ctx.dialect))
            val identifierAttributeSnapshotId =
                requireNotNull(rsEntities.getUuid("identifier_attribute_snapshot_id", ctx.dialect))
            connection.prepareStatement(
                "INSERT INTO model_entity_pk_snapshot (id, lineage_id, model_entity_snapshot_id) VALUES (?, ?, ?)"
            ).use { stmt ->
                stmt.setUUID(1, entitySnapshotId, ctx.dialect)
                stmt.setUUID(2, entityLineageId, ctx.dialect)
                stmt.setUUID(3, entitySnapshotId, ctx.dialect)
                stmt.executeUpdate()
            }
            connection.prepareStatement(
                "INSERT INTO model_entity_pk_attribute_snapshot (model_entity_pk_snapshot_id, priority, model_entity_attribute_snapshot_id) VALUES (?, ?, ?)"
            ).use { stmt ->
                stmt.setUUID(1, entitySnapshotId, ctx.dialect)
                stmt.setInt(2, DEFAULT_IDENTIFIER_ATTRIBUTE_PRIORITY)
                stmt.setUUID(3, identifierAttributeSnapshotId, ctx.dialect)
                stmt.executeUpdate()
            }
        }
    }

    companion object {
        private const val DEFAULT_IDENTIFIER_ATTRIBUTE_PRIORITY = 0

    }
}
