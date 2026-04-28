package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.LocalizedText
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@EnableDatabaseTests
class Model_UpdateName_Test {

    @Test
    fun `updates on model name persists the name`() {
        val env = TestEnvOneModel()
        env.dispatch(
            ModelAction.Model_UpdateName(
                modelRef = env.modelRef,
                value = LocalizedText("Model name 2")
            )
        )
        assertEquals(LocalizedText("Model name 2"), env.query.findModelAggregate(env.modelRef).name)
    }
}