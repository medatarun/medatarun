package io.medatarun.model.domain.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedTextNotLocalized
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
}