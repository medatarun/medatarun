package io.medatarun.model.domain.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedMarkdownNotLocalized
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelNotFoundException
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.ports.exposed.ModelQueries
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Model_UpdateX_Test {
    @Test
    fun `updates on model fails if model not found`() {
        val env = createEnv()
        val query: ModelQueries = env.queries

        val modelKey = ModelKey("m1")
        env.dispatch(
            ModelAction.Model_Create(
                modelKey = modelKey,
                name = LocalizedTextNotLocalized("Model name"),
                description = null,
                version = ModelVersion("2.0.0")
            )
        )
        val modelKeyWrong = modelRefKey("m2")
        assertFailsWith(ModelNotFoundException::class) {
            env.dispatch(
                ModelAction.Model_UpdateKey(
                    modelRef = modelKeyWrong,
                    value = ModelKey("m2")
                )
            )
        }
        assertFailsWith(ModelNotFoundException::class) {
            env.dispatch(
                ModelAction.Model_UpdateName(
                    modelRef = modelKeyWrong,
                    value = LocalizedTextNotLocalized("other")
                )
            )
        }
        assertFailsWith(ModelNotFoundException::class) {
            env.dispatch(
                ModelAction.Model_UpdateDescription(
                    modelRef = modelKeyWrong,
                    value = LocalizedMarkdownNotLocalized("other description")
                )
            )
        }
        assertFailsWith(ModelNotFoundException::class) {
            env.dispatch(
                ModelAction.Model_UpdateVersion(
                    modelRef = modelKeyWrong,
                    value = ModelVersion("3.0.0")
                )
            )
        }
        env.dispatch(
            ModelAction.Model_UpdateName(
                modelRef = modelRefKey(modelKey),
                value = LocalizedTextNotLocalized("Model name 2")
            )
        )
        assertEquals(LocalizedTextNotLocalized("Model name 2"), query.findModelByKey(modelKey).name)
    }

}
