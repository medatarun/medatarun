package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.model.ports.exposed.ModelQueries
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@EnableDatabaseTests
class Model_UpdateX_Test {
    @Test
    fun `updates on model fails if model not found`() {
        val env = ModelTestEnv()
        val query: ModelQueries = env.queries

        val modelRef = modelRefKey("m1")
        env.dispatch(
            ModelAction.Model_Create(
                key = modelRef.key,
                name = LocalizedText("Model name"),
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
                    value = LocalizedText("other")
                )
            )
        }
        assertFailsWith(ModelNotFoundException::class) {
            env.dispatch(
                ModelAction.Model_UpdateDescription(
                    modelRef = modelKeyWrong,
                    value = LocalizedMarkdown("other description")
                )
            )
        }
        assertFailsWith(ModelNotFoundException::class) {
            env.dispatch(
                ModelAction.Model_Release(
                    modelRef = modelKeyWrong,
                    value = ModelVersion("3.0.0")
                )
            )
        }
        assertFailsWith(ModelNotFoundException::class) {
            env.dispatch(
                ModelAction.Model_UpdateAuthority(
                    modelRef = modelKeyWrong,
                    value = ModelAuthority.CANONICAL
                )
            )
        }
        env.dispatch(
            ModelAction.Model_UpdateName(
                modelRef = modelRef,
                value = LocalizedText("Model name 2")
            )
        )
        assertEquals(LocalizedText("Model name 2"), query.findModelRoot(modelRef).name)
    }

}
