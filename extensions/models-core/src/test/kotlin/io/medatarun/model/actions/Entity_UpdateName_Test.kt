package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelVersion
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@EnableDatabaseTests
class Entity_UpdateName_Test {

    @Test
    fun `update entity name not null persisted`() {
        val env = TestEnvEntityUpdate()
        val newName = LocalizedTextNotLocalized("Entity primary updated")

        env.dispatch(ModelAction.Entity_UpdateName(env.modelRef, env.primaryEntityRef, newName))

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertEquals(newName, reloaded.name)
    }

    @Test
    fun `update entity name null then name is null`() {
        val env = TestEnvEntityUpdate()

        env.dispatch(ModelAction.Entity_UpdateName(env.modelRef, env.primaryEntityRef, null))

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertNull(reloaded.name)
    }

    @Test
    fun `update entity name after release persists on current model`() {
        val env = TestEnvEntityUpdate()
        val newName = LocalizedTextNotLocalized("Entity primary after release")

        env.dispatch(ModelAction.Model_Release(env.modelRef, ModelVersion("1.1.0")))
        env.dispatch(ModelAction.Entity_UpdateName(env.modelRef, env.primaryEntityRef, newName))

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertEquals(newName, reloaded.name)
    }
}
