package io.medatarun.model.infra.db

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.*
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.model.infra.db.tables.ModelEventTable
import io.medatarun.model.infra.db.tables.ModelSnapshotTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull



@EnableDatabaseTests
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

        val model = env.queries.findModelAggregate(ModelRef.ByKey(ModelKey("crm")))

        env.dispatch(
            ModelAction.Model_UpdateName(
                modelRef = ModelRef.ById(model.id),
                value = LocalizedTextNotLocalized("CRM v2")
            )
        )

        val rows = env.findAllModelEvents(model.id)

        assertEquals(3, rows.size)

        assertEquals(1, rows[0].eventSequenceNumber)
        assertEquals("model_created", rows[0].eventType)
        assertEquals(env.principal.id, rows[0].traceabilityRecord.actorId)
        assertNotNull(rows[0].traceabilityRecord.origin)

        assertEquals(2, rows[1].eventSequenceNumber)
        assertEquals("model_release", rows[1].eventType)
        assertEquals(env.principal.id, rows[1].traceabilityRecord.actorId)
        assertNotNull(rows[1].traceabilityRecord.origin)

        assertEquals(3, rows[2].eventSequenceNumber)
        assertEquals("model_name_updated", rows[2].eventType)
        assertEquals(env.principal.id, rows[2].traceabilityRecord.actorId)
        assertNotNull(rows[2].traceabilityRecord.origin)
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

        val model = env.queries.findModelRoot(ModelRef.ByKey(ModelKey("crm")))

        val modelRefId = ModelRef.ById(model.id)
        env.dispatch(
            ModelAction.Model_Release(
                modelRef = modelRefId,
                value = ModelVersion("2.0.0")
            )
        )

        val rows = env.findAllModelEvents(model.id)

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
