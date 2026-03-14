package io.medatarun.model.infra.db

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelRef
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.domain.fixtures.ModelTestEnv
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

        assertEquals(2, rows.size)
        assertEquals(1, rows[0].streamRevision)
        assertEquals(2, rows[1].streamRevision)
        assertEquals("model_created", rows[0].eventType)
        assertEquals("model_name_updated", rows[1].eventType)
        assertEquals(ModelTestEnv.testPrincipal.id, rows[0].actorId)
        assertEquals(ModelTestEnv.testPrincipal.id, rows[1].actorId)
        assertNotNull(rows[0].actionId)
        assertNotNull(rows[1].actionId)
    }
}
