package io.medatarun.model.actions

import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeNotFoundException
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@EnableDatabaseTests
class EntityAttribute_UpdateType_Test {

    @Test
    fun `update attribute type is persisted`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-type")
        val entityRef = entityRefKey("entity-a")
        val typeStringRef = typeRefKey("String")
        val typeMarkdownRef = typeRefKey("Markdown")
        val attributeRef = entityAttributeRefKey("myattribute")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeStringRef.key)
        env.typeCreate(modelRef, typeMarkdownRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeStringRef,
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.EntityAttribute_UpdateType(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                value = typeMarkdownRef
            )
        )

        env.replayWithRebuild {
            val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
            val typeMarkdown = env.queries.findType(modelRef, typeMarkdownRef)
            assertEquals(typeMarkdown.id, reloaded.typeId)
        }
    }

    @Test
    fun `update attribute unknown type then error`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-type-unknown")
        val entityRef = entityRefKey("entity-a")
        val typeStringRef = typeRefKey("String")
        val unknownTypeRef = typeRefKey("String2")
        val attributeRef = entityAttributeRefKey("myattribute")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeStringRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeStringRef,
                optional = false,
                name = null,
                description = null
            )
        )

        assertThrows<TypeNotFoundException> {
            env.dispatch(
                ModelAction.EntityAttribute_UpdateType(
                    modelRef = modelRef,
                    entityRef = entityRef,
                    attributeRef = attributeRef,
                    value = unknownTypeRef
                )
            )
        }
    }

    @Test
    fun `update attribute type with same value does not create new event`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-type-noop")
        val entityRef = entityRefKey("entity-a")
        val typeStringRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("myattribute")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeStringRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeStringRef,
                optional = false,
                name = null,
                description = null
            )
        )

        val beforeEventId = env.findLastStoredModelChangeEvent(modelRef).eventId
        env.dispatch(
            ModelAction.EntityAttribute_UpdateType(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                value = typeStringRef
            )
        )
        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        assertEquals(beforeEventId, afterEventId)
    }
}
