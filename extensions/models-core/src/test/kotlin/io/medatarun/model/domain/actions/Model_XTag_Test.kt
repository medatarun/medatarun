package io.medatarun.model.domain.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelVersion
import io.medatarun.tags.core.domain.TagAttachScopeMismatchException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class Model_XTag_Test {

    @Test
    fun `add and delete tag on model persists tag ids`() {
        val env = TestEnvOneModel()
        val managedTag = env.runtime.createManagedTag("g-model", "t-model")

        env.dispatch(ModelAction.Model_AddTag(env.modelRef, managedTag.ref))
        assertEquals(listOf(managedTag.id), env.query.findModel(env.modelRef).tags)

        env.dispatch(ModelAction.Model_DeleteTag(env.modelRef, managedTag.ref))
        assertTrue(env.query.findModel(env.modelRef).tags.isEmpty())
    }

    @Test
    fun `add local tag of same model on model persists tag ids`() {
        val env = TestEnvOneModel()
        val localTag = env.runtime.createFreeTagInModelScope(env.modelRef, "local-model-tag")

        env.dispatch(ModelAction.Model_AddTag(env.modelRef, localTag.ref))
        assertEquals(listOf(localTag.id), env.query.findModel(env.modelRef).tags)
    }

    @Test
    fun `add local tag of another model on model then error`() {
        val env = TestEnvOneModel()
        val foreignModelRef = modelRefKey("m2")
        env.runtime.dispatch(
            ModelAction.Model_Create(
                modelKey = ModelKey("m2"),
                name = LocalizedTextNotLocalized("Model 2"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        val foreignTag = env.runtime.createFreeTagInModelScope(foreignModelRef, "foreign-model-tag")

        assertFailsWith<TagAttachScopeMismatchException> {
            env.dispatch(ModelAction.Model_AddTag(env.modelRef, foreignTag.ref))
        }
    }
}