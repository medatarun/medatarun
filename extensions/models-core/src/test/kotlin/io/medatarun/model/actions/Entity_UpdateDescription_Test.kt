package io.medatarun.model.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedMarkdownNotLocalized
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Entity_UpdateDescription_Test {

    @Test
    fun `update entity description not null persisted`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvEntityUpdate()
        val newDescription = LocalizedMarkdownNotLocalized("Primary entity updated description")

        env.dispatch(ModelAction.Entity_UpdateDescription(env.modelRef, env.primaryEntityRef, newDescription))

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertEquals(newDescription, reloaded.description)
    }

    @Test
    fun `update entity description with null then description is null`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvEntityUpdate()

        env.dispatch(ModelAction.Entity_UpdateDescription(env.modelRef, env.primaryEntityRef, null))

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertNull(reloaded.description)
    }
}