package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.LocalizedMarkdownNotLocalized
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@EnableDatabaseTests
class Entity_UpdateDescription_Test {

    @Test
    fun `update entity description not null persisted`() {
        val env = TestEnvEntityUpdate()
        val newDescription = LocalizedMarkdownNotLocalized("Primary entity updated description")

        env.dispatch(ModelAction.Entity_UpdateDescription(env.modelRef, env.primaryEntityRef, newDescription))

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertEquals(newDescription, reloaded.description)
    }

    @Test
    fun `update entity description with null then description is null`() {
        val env = TestEnvEntityUpdate()

        env.dispatch(ModelAction.Entity_UpdateDescription(env.modelRef, env.primaryEntityRef, null))

        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertNull(reloaded.description)
    }
}