package io.medatarun.model.actions

import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelVersion
import io.medatarun.tags.core.domain.TagAttachScopeMismatchException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class Entity_XTag_Test {


    @Test
    fun `add and delete tag on entity persists tag ids`() {
        val env = TestEnvEntityUpdate()
        val globalTag = env.runtime.createGlobalTag("g-entity", "t-entity")

        env.dispatch(ModelAction.Entity_AddTag(env.modelRef, env.primaryEntityRef, globalTag.ref))
        assertEquals(listOf(globalTag.id), env.query.findEntity(env.modelRef, env.primaryEntityRef).tags)

        env.dispatch(ModelAction.Entity_DeleteTag(env.modelRef, env.primaryEntityRef, globalTag.ref))
        assertTrue(env.query.findEntity(env.modelRef, env.primaryEntityRef).tags.isEmpty())
    }

    @Test
    fun `add local tag of same model on entity persists tag ids`() {
        val env = TestEnvEntityUpdate()
        val localTag = env.runtime.createLocalTagInModelScope(env.modelRef, "local-entity-tag")

        env.dispatch(ModelAction.Entity_AddTag(env.modelRef, env.primaryEntityRef, localTag.ref))
        assertEquals(listOf(localTag.id), env.query.findEntity(env.modelRef, env.primaryEntityRef).tags)
    }

    @Test
    fun `add local tag of another model on entity then error`() {
        val env = TestEnvEntityUpdate()
        val foreignModelRef = modelRefKey("model-entity-update-2")
        env.runtime.dispatch(
            ModelAction.Model_Create(
                key = ModelKey("model-entity-update-2"),
                name = LocalizedTextNotLocalized("Model entity update 2"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        val foreignTag = env.runtime.createLocalTagInModelScope(foreignModelRef, "foreign-entity-tag")

        assertFailsWith<TagAttachScopeMismatchException> {
            env.dispatch(ModelAction.Entity_AddTag(env.modelRef, env.primaryEntityRef, foreignTag.ref))
        }
    }
}