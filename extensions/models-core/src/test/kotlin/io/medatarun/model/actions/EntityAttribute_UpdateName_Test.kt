package io.medatarun.model.actions

import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@EnableDatabaseTests
class EntityAttribute_UpdateName_Test {

    @Test
    fun `update attribute name is persisted`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-name")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("myattribute")
        val nextValue = LocalizedText("New name")

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
            ModelAction.EntityAttribute_UpdateName(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                value = nextValue
            )
        )

        env.replayWithRebuild {
            val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
            assertEquals(nextValue, reloaded.name)
        }
    }

    @Test
    fun `update attribute name to null stays null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-name-null")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("myattribute")

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
                name = LocalizedText("Name"),
                description = null
            )
        )

        env.dispatch(
            ModelAction.EntityAttribute_UpdateName(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                value = null
            )
        )

        val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
        assertNull(reloaded.name)
    }

    @Test
    fun `update attribute name with same value does not create new event`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-name-noop")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("myattribute")
        val name = LocalizedText("My name")

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
                name = name,
                description = null
            )
        )

        val beforeEventId = env.findLastStoredModelChangeEvent(modelRef).eventId
        env.dispatch(
            ModelAction.EntityAttribute_UpdateName(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                value = name
            )
        )
        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        assertEquals(beforeEventId, afterEventId)
    }
}
