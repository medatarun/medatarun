package io.medatarun.model.actions

import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.LocalizedMarkdownNotLocalized
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@EnableDatabaseTests
class EntityAttribute_UpdateDescription_Test {

    @Test
    fun `update attribute description is persisted`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-description")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("myattribute")
        val nextValue = LocalizedMarkdownNotLocalized("New description")

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
            ModelAction.EntityAttribute_UpdateDescription(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                value = nextValue
            )
        )

        env.replayWithRebuild {
            val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
            assertEquals(nextValue, reloaded.description)
        }
    }

    @Test
    fun `update attribute description to null stays null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-description-null")
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
                name = null,
                description = LocalizedMarkdownNotLocalized("New description")
            )
        )

        env.dispatch(
            ModelAction.EntityAttribute_UpdateDescription(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                value = null
            )
        )

        val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
        assertNull(reloaded.description)
    }

    @Test
    fun `update attribute description with same value does not create new event`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-description-noop")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("myattribute")
        val description = LocalizedMarkdownNotLocalized("My description")

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
                description = description
            )
        )

        val beforeEventId = env.findLastStoredModelChangeEvent(modelRef).eventId
        env.dispatch(
            ModelAction.EntityAttribute_UpdateDescription(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                value = description
            )
        )
        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        assertEquals(beforeEventId, afterEventId)
    }
}
