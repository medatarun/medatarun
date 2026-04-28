package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.LocalizedMarkdown
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@EnableDatabaseTests
class Model_UpdateDescription_Test {

    @Test
    fun `updates on model description persists the description`() {
        val env = TestEnvOneModel()
        env.dispatch(
            ModelAction.Model_UpdateDescription(
                modelRef = env.modelRef,
                value = LocalizedMarkdown("Model description 2")
            )
        )
        assertEquals(
            LocalizedMarkdown("Model description 2"),
            env.query.findModelAggregate(env.modelRef).description
        )
    }

    @Test
    fun `updates on model description to null persists the description`() {
        val env = TestEnvOneModel()
        env.dispatch(
            ModelAction.Model_UpdateDescription(
                modelRef = env.modelRef,
                value = LocalizedMarkdown("Model description 2")
            )
        )
        env.dispatch(ModelAction.Model_UpdateDescription(env.modelRef, null))
        assertNull(env.query.findModelAggregate(env.modelRef).description)
    }

}