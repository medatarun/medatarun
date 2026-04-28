package io.medatarun.model.actions

import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.tags.core.domain.TagAttachScopeMismatchException
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@EnableDatabaseTests
class EntityAttribute_XTag_Test {

    @Test
    fun `add and delete tag on entity attribute persists tag ids`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-xtag")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("tagged")
        val globalTag = env.createGlobalTag("g-ea", "t-ea")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeRef,
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.EntityAttribute_AddTag(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                tag = globalTag.ref
            )
        )

        env.replayWithRebuild {
            val added = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
            assertEquals(listOf(globalTag.id), added.tags)
        }

        env.dispatch(
            ModelAction.EntityAttribute_DeleteTag(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                tag = globalTag.ref
            )
        )

        val deleted = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
        assertTrue(deleted.tags.isEmpty())
    }

    @Test
    fun `add local tag of same model on entity attribute persists tag ids`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-xtag-local")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("tagged")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeRef,
                optional = false,
                name = null,
                description = null
            )
        )
        val localTag = env.createLocalTagInModelScope(modelRef, "local-ea-tag")

        env.dispatch(
            ModelAction.EntityAttribute_AddTag(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                tag = localTag.ref
            )
        )

        val added = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
        assertEquals(listOf(localTag.id), added.tags)
    }

    @Test
    fun `add local tag of another model on entity attribute then error`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-xtag-foreign")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("tagged")
        val foreignModelRef = modelRefKey("sample-model-2")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeRef,
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.Model_Create(
                key = ModelKey("sample-model-2"),
                name = io.medatarun.model.domain.LocalizedText("Sample model 2"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        val foreignTag = env.createLocalTagInModelScope(foreignModelRef, "foreign-ea-tag")

        assertFailsWith<TagAttachScopeMismatchException> {
            env.dispatch(
                ModelAction.EntityAttribute_AddTag(
                    modelRef = modelRef,
                    entityRef = entityRef,
                    attributeRef = attributeRef,
                    tag = foreignTag.ref
                )
            )
        }
    }
}
