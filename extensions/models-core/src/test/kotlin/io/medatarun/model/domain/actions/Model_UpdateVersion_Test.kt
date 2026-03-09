package io.medatarun.model.domain.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.ModelVersion
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Model_UpdateVersion_Test {

    @Test
    fun `updates on model version persists the version`() {
        val env = TestEnvOneModel()
        env.dispatch(ModelAction.Model_UpdateVersion(env.modelRef, ModelVersion("4.5.6")))
        assertEquals(ModelVersion("4.5.6"), env.query.findModel(env.modelRef).version)
    }
}