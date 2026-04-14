package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelReleaseVersionMustBeGreaterThanPreviousException
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.domain.fixtures.ModelTestEnv
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@EnableDatabaseTests
class Model_Release_Test {

    @Test
    fun `updates on model version persists the version`() {
        val env = ModelTestEnv()
        val modelKey = ModelKey("release-version-persist")
        val modelRef = modelRefKey(modelKey)

        env.dispatch(
            ModelAction.Model_Create(
                key = modelKey,
                name = LocalizedTextNotLocalized("Model release"),
                description = null,
                version = ModelVersion("4.5.5")
            )
        )

        env.dispatch(ModelAction.Model_Release(modelRef, ModelVersion("4.5.6")))
        env.replayWithRebuild {
            assertEquals(ModelVersion("4.5.6"), env.queries.findModelAggregate(modelRef).version)
        }
    }

    @Test
    fun `release throws when version already exists for the same model`() {
        val env = ModelTestEnv()
        val modelKey = ModelKey("release-duplicate-version")
        val modelRef = modelRefKey(modelKey)

        env.dispatch(
            ModelAction.Model_Create(
                key = modelKey,
                name = LocalizedTextNotLocalized("Model release duplicate"),
                description = null,
                version = ModelVersion("4.5.5")
            )
        )

        assertFailsWith<ModelReleaseVersionMustBeGreaterThanPreviousException> {
            env.dispatch(ModelAction.Model_Release(modelRef, ModelVersion("4.5.5")))
        }
    }

    @Test
    fun `release throws when version is lower than the previous one`() {
        val env = ModelTestEnv()
        val modelKey = ModelKey("release-lower-version")
        val modelRef = modelRefKey(modelKey)

        env.dispatch(
            ModelAction.Model_Create(
                key = modelKey,
                name = LocalizedTextNotLocalized("Model release lower"),
                description = null,
                version = ModelVersion("4.5.5")
            )
        )

        assertFailsWith<ModelReleaseVersionMustBeGreaterThanPreviousException> {
            env.dispatch(ModelAction.Model_Release(modelRef, ModelVersion("4.5.4")))
        }
    }

    @Test
    fun `release without content change is allowed`() {
        val env = createEnv()
        val modelKey = ModelKey("m1")

        env.dispatch(
            ModelAction.Model_Create(
                key = modelKey,
                name = LocalizedTextNotLocalized("Model name"),
                description = null,
                version = ModelVersion("4.5.5")
            )
        )

        env.dispatch(ModelAction.Model_Release(modelRefKey(modelKey), ModelVersion("4.5.6")))
        assertEquals(ModelVersion("4.5.6"), env.queries.findModelAggregateByKey(modelKey).version)
    }
}
