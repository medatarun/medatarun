package io.medatarun.model.infra.db

import io.medatarun.actions.adapters.ActionTraceabilityRecord
import io.medatarun.actions.domain.ActionInstanceId
import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.*
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.model.infra.db.tables.ModelEventTable
import io.medatarun.model.infra.db.tables.ModelSnapshotTable
import io.medatarun.model.internal.ModelCmdCopyImpl
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmdEnveloppe
import io.medatarun.model.ports.exposed.ModelCmds
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// TODO this is not correct, see val modelCmds = env.platform.services.getService(ModelCmds::class) in the last test shall not happen
class ModelEventLogTest {

    @Test
    fun `create and update model append model events with action metadata`() {
        val env = ModelTestEnv()

        env.dispatch(
            ModelAction.Model_Create(
                key = ModelKey("crm"),
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
        assertNotNull(rows[0].traceabilityOrigin)
        assertNotNull(rows[1].traceabilityOrigin)
        assertNotNull(rows[2].traceabilityOrigin)
    }

    @Test
    fun `update model version appends a model_release event with model_version`() {
        val env = ModelTestEnv()

        env.dispatch(
            ModelAction.Model_Create(
                key = ModelKey("crm"),
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
        assertEquals(ModelVersion("2.0.0"), rows[2].modelVersion)

        env.dbConnectionFactory.withExposed {
            val currentHeadRows = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.modelId eq model.id) and (ModelSnapshotTable.snapshotKind eq ModelSnapshotKind.CURRENT_HEAD)
            }.toList()
            val versionSnapshotRows = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.modelId eq model.id) and (ModelSnapshotTable.snapshotKind eq ModelSnapshotKind.VERSION_SNAPSHOT)
            }.toList()
            val releaseSnapshotRows = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.modelId eq model.id) and
                        (ModelSnapshotTable.snapshotKind eq ModelSnapshotKind.VERSION_SNAPSHOT) and
                        (ModelSnapshotTable.version eq ModelVersion("2.0.0"))
            }.toList()
            val releaseEventId = ModelEventTable.select(ModelEventTable.id).where {
                (ModelEventTable.modelId eq model.id) and
                        (ModelEventTable.eventType eq "model_release") and
                        (ModelEventTable.modelVersion eq ModelVersion("2.0.0"))
            }.single()[ModelEventTable.id]

            assertEquals(1, currentHeadRows.size)
            assertEquals(2, versionSnapshotRows.size)
            assertEquals(1, releaseSnapshotRows.size)
            assertEquals(3, currentHeadRows.single()[ModelSnapshotTable.upToRevision])
            assertEquals(ModelVersion("2.0.0"), releaseSnapshotRows.single()[ModelSnapshotTable.version])
            assertEquals(3, releaseSnapshotRows.single()[ModelSnapshotTable.upToRevision])
            assertEquals(releaseEventId, releaseSnapshotRows.single()[ModelSnapshotTable.modelEventReleaseId])
        }
    }

}
