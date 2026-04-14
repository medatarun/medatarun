package io.medatarun.model.actions

import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.tags.core.domain.TagAttachScopeMismatchException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@EnableDatabaseTests
class Entity_XTag_Test {

    @Test
    fun `add and delete tag on entity persists tag ids`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-xtag")
        val entityRef = entityRefKey("entity-primary")
        val typeRef = typeRefKey("String")
        val globalTag = env.createGlobalTag("g-entity", "t-entity")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate2(modelRef, entityRef.key, LocalizedTextNotLocalized("Entity primary"))

        env.dispatch(
            ModelAction.Entity_AddTag(
                modelRef = modelRef,
                entityRef = entityRef,
                tag = globalTag.ref
            )
        )

        env.replayWithRebuild {
            assertEquals(listOf(globalTag.id), env.queries.findEntity(modelRef, entityRef).tags)
        }

        env.dispatch(
            ModelAction.Entity_DeleteTag(
                modelRef = modelRef,
                entityRef = entityRef,
                tag = globalTag.ref
            )
        )
        assertTrue(env.queries.findEntity(modelRef, entityRef).tags.isEmpty())
    }

    @Test
    fun `add local tag of same model on entity persists tag ids`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-xtag-local")
        val entityRef = entityRefKey("entity-primary")
        val typeRef = typeRefKey("String")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate2(modelRef, entityRef.key, LocalizedTextNotLocalized("Entity primary"))
        val localTag = env.createLocalTagInModelScope(modelRef, "local-entity-tag")

        env.dispatch(
            ModelAction.Entity_AddTag(
                modelRef = modelRef,
                entityRef = entityRef,
                tag = localTag.ref
            )
        )
        assertEquals(listOf(localTag.id), env.queries.findEntity(modelRef, entityRef).tags)
    }

    @Test
    fun `add local tag of another model on entity then error`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-xtag-foreign")
        val entityRef = entityRefKey("entity-primary")
        val typeRef = typeRefKey("String")
        val foreignModelRef = modelRefKey("model-entity-update-2")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate2(modelRef, entityRef.key, LocalizedTextNotLocalized("Entity primary"))
        env.dispatch(
            ModelAction.Model_Create(
                key = ModelKey("model-entity-update-2"),
                name = LocalizedTextNotLocalized("Model entity update 2"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        val foreignTag = env.createLocalTagInModelScope(foreignModelRef, "foreign-entity-tag")

        assertFailsWith<TagAttachScopeMismatchException> {
            env.dispatch(
                ModelAction.Entity_AddTag(
                    modelRef = modelRef,
                    entityRef = entityRef,
                    tag = foreignTag.ref
                )
            )
        }
    }
}
