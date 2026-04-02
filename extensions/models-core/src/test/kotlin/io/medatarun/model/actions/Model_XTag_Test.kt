package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelVersion
import io.medatarun.tags.core.domain.TagAttachScopeMismatchException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@EnableDatabaseTests
class Model_XTag_Test {

    @Test
    fun `add and delete tag on model persists tag ids`() {
        val env = TestEnvOneModel()
        val globalTag = env.runtime.createGlobalTag("g-model", "t-model")

        env.dispatch(ModelAction.Model_AddTag(env.modelRef, globalTag.ref))
        assertEquals(listOf(globalTag.id), env.query.findModel(env.modelRef).tags)

        env.dispatch(ModelAction.Model_DeleteTag(env.modelRef, globalTag.ref))
        assertTrue(env.query.findModel(env.modelRef).tags.isEmpty())
    }

    @Test
    fun `add local tag of same model on model persists tag ids`() {
        val env = TestEnvOneModel()
        val localTag = env.runtime.createLocalTagInModelScope(env.modelRef, "local-model-tag")

        env.dispatch(ModelAction.Model_AddTag(env.modelRef, localTag.ref))
        assertEquals(listOf(localTag.id), env.query.findModel(env.modelRef).tags)
    }

    @Test
    fun `add local tag of another model on model then error`() {
        val env = TestEnvOneModel()
        val foreignModelRef = modelRefKey("m2")
        env.runtime.dispatch(
            ModelAction.Model_Create(
                key = ModelKey("m2"),
                name = LocalizedTextNotLocalized("Model 2"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        val foreignTag = env.runtime.createLocalTagInModelScope(foreignModelRef, "foreign-model-tag")

        assertFailsWith<TagAttachScopeMismatchException> {
            env.dispatch(ModelAction.Model_AddTag(env.modelRef, foreignTag.ref))
        }
    }
}