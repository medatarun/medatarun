package io.medatarun.model.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedTextNotLocalized
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Model_UpdateName_Test {



    @Test
    fun `updates on model name persists the name`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvOneModel()
        env.dispatch(
            ModelAction.Model_UpdateName(
                modelRef = env.modelRef,
                value = LocalizedTextNotLocalized("Model name 2")
            )
        )
        assertEquals(LocalizedTextNotLocalized("Model name 2"), env.query.findModel(env.modelRef).name)
    }
}