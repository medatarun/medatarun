package io.medatarun.model.infra.db

import io.medatarun.actions.domain.ActionInstanceId
import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelRef
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.model.internal.ModelCmdCopyImpl
import io.medatarun.model.infra.db.tables.ModelEventTable
import io.medatarun.model.infra.db.tables.ModelSnapshotTable
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmdEnveloppe
import io.medatarun.model.ports.exposed.ModelCmds
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ModelEventLogTest {

    @Test
    fun `create and update model append model events with action metadata`() {
        val env = ModelTestEnv()

        env.dispatch(
            ModelAction.Model_Create(
                modelKey = ModelKey("crm"),
                name = LocalizedTextNotLocalized("CRM"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )

        val model = env.queries.findModel(ModelRef.ByKey(ModelKey("crm")))

        env.dispatch(
            ModelAction.Model_UpdateName(
                modelRef = ModelRef.ById(model.id),
                value = LocalizedTextNotLocalized("CRM v2")
            )
        )

        val rows = env.storageDb.findAllModelEvents(model.id)

        assertEquals(3, rows.size)
        assertEquals(1, rows[0].streamRevision)
        assertEquals(2, rows[1].streamRevision)
        assertEquals(3, rows[2].streamRevision)
        assertEquals("model_created", rows[0].eventType)
        assertEquals("model_release", rows[1].eventType)
        assertEquals("model_name_updated", rows[2].eventType)
        assertEquals(ModelTestEnv.testPrincipal.id, rows[0].actorId)
        assertEquals(ModelTestEnv.testPrincipal.id, rows[1].actorId)
        assertEquals(ModelTestEnv.testPrincipal.id, rows[2].actorId)
        assertNotNull(rows[0].actionId)
        assertNotNull(rows[1].actionId)
        assertNotNull(rows[2].actionId)
    }

    @Test
    fun `update model version appends a model_release event with model_version`() {
        val env = ModelTestEnv()

        env.dispatch(
            ModelAction.Model_Create(
                modelKey = ModelKey("crm"),
                name = LocalizedTextNotLocalized("CRM"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )

        val model = env.queries.findModel(ModelRef.ByKey(ModelKey("crm")))

        env.dispatch(
            ModelAction.Model_Release(
                modelRef = ModelRef.ById(model.id),
                value = ModelVersion("2.0.0")
            )
        )

        val rows = env.storageDb.findAllModelEvents(model.id)

        assertEquals(3, rows.size)
        assertEquals("model_release", rows[2].eventType)
        assertEquals("2.0.0", rows[2].modelVersion)

        env.dbConnectionFactory.withExposed {
            val currentHeadRows = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.modelId eq model.id) and (ModelSnapshotTable.snapshotKind eq "CURRENT_HEAD")
            }.toList()
            val versionSnapshotRows = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.modelId eq model.id) and (ModelSnapshotTable.snapshotKind eq "VERSION_SNAPSHOT")
            }.toList()
            val releaseSnapshotRows = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.modelId eq model.id) and
                    (ModelSnapshotTable.snapshotKind eq "VERSION_SNAPSHOT") and
                    (ModelSnapshotTable.version eq "2.0.0")
            }.toList()
            val releaseEventId = ModelEventTable.select(ModelEventTable.id).where {
                (ModelEventTable.modelId eq model.id) and
                    (ModelEventTable.eventType eq "model_release") and
                    (ModelEventTable.modelVersion eq "2.0.0")
            }.single()[ModelEventTable.id]

            assertEquals(1, currentHeadRows.size)
            assertEquals(2, versionSnapshotRows.size)
            assertEquals(1, releaseSnapshotRows.size)
            assertEquals(3, currentHeadRows.single()[ModelSnapshotTable.upToRevision])
            assertEquals("2.0.0", releaseSnapshotRows.single()[ModelSnapshotTable.version])
            assertEquals(3, releaseSnapshotRows.single()[ModelSnapshotTable.upToRevision])
            assertEquals(releaseEventId, releaseSnapshotRows.single()[ModelSnapshotTable.modelEventReleaseId])
        }
    }

    @Test
    fun `import model appends an initial model_release event`() {
        val env = ModelTestEnv()

        env.dispatch(
            ModelAction.Model_Create(
                modelKey = ModelKey("source"),
                name = LocalizedTextNotLocalized("Source"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )

        val source = env.queries.findModel(ModelRef.ByKey(ModelKey("source")))
        val imported = ModelCmdCopyImpl().copy(source, ModelKey("imported"))
        val modelCmds = env.platform.services.getService(ModelCmds::class)

        modelCmds.dispatch(
            ModelCmdEnveloppe(
                actionId = ActionInstanceId(UUID.randomUUID()),
                principal = ModelTestEnv.testPrincipal,
                cmd = ModelCmd.ImportModel(imported, emptyList())
            )
        )

        val rows = env.storageDb.findAllModelEvents(imported.id)

        assertEquals("model_aggregate_stored", rows[0].eventType)
        assertEquals("model_release", rows[1].eventType)
        assertEquals(imported.version.value, rows[1].modelVersion)
    }
}
