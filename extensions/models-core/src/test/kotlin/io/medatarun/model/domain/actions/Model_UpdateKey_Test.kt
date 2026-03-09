package io.medatarun.model.domain.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelDuplicateKeyException
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelVersion
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class Model_UpdateKey_Test {

    @Test
    fun `updates on model key persists the key`() {
        val env = TestEnvOneModel()
        val oldKey = ModelKey("m1")
        val newKey = ModelKey("m1-renamed")

        env.dispatch(
            ModelAction.Model_UpdateKey(
                modelRef = env.modelRef,
                value = newKey
            )
        )

        assertNull(env.query.findModelOptional(modelRefKey(oldKey)))
        assertEquals(newKey, env.query.findModelByKey(newKey).key)
    }

    @Test
    fun `updates on model key throws when key is already used by another model`() {
        val env = createEnv()
        val firstModelKey = ModelKey("m1")
        val secondModelKey = ModelKey("m2")

        env.dispatch(
            ModelAction.Model_Create(
                modelKey = firstModelKey,
                name = LocalizedTextNotLocalized("Model 1"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                modelKey = secondModelKey,
                name = LocalizedTextNotLocalized("Model 2"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )

        assertFailsWith<ModelDuplicateKeyException> {
            env.dispatch(
                ModelAction.Model_UpdateKey(
                    modelRef = modelRefKey(firstModelKey),
                    value = secondModelKey
                )
            )
        }
    }
}
