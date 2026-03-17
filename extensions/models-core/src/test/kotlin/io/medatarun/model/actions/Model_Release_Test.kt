package io.medatarun.model.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelReleaseVersionMustBeGreaterThanPreviousException
import io.medatarun.model.domain.ModelVersion
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Model_Release_Test {

    @Test
    fun `updates on model version persists the version`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvOneModel(ModelVersion("4.5.5"))
        env.dispatch(ModelAction.Model_Release(env.modelRef, ModelVersion("4.5.6")))
        assertEquals(ModelVersion("4.5.6"), env.query.findModel(env.modelRef).version)
    }

    @Test
    fun `release throws when version already exists for the same model`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvOneModel(ModelVersion("4.5.5"))

        assertFailsWith<ModelReleaseVersionMustBeGreaterThanPreviousException> {
            env.dispatch(ModelAction.Model_Release(env.modelRef, ModelVersion("4.5.5")))
        }
    }

    @Test
    fun `release throws when version is lower than the previous one`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvOneModel(ModelVersion("4.5.5"))

        assertFailsWith<ModelReleaseVersionMustBeGreaterThanPreviousException> {
            env.dispatch(ModelAction.Model_Release(env.modelRef, ModelVersion("4.5.4")))
        }
    }

    @Test
    fun `release without content change is allowed`() {
        val env = _root_ide_package_.io.medatarun.model.actions.createEnv()
        val modelKey = ModelKey("m1")

        env.dispatch(
            ModelAction.Model_Create(
                modelKey = modelKey,
                name = LocalizedTextNotLocalized("Model name"),
                description = null,
                version = ModelVersion("4.5.5")
            )
        )

        env.dispatch(ModelAction.Model_Release(modelRefKey(modelKey), ModelVersion("4.5.6")))
        assertEquals(ModelVersion("4.5.6"), env.queries.findModelByKey(modelKey).version)
    }
}
