package io.medatarun.model.actions

import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@EnableDatabaseTests
class EntityAttribute_UpdateOptional_Test {

    @Test
    fun `update attribute optional true to false is persisted`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-optional-false")
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
                optional = true,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.EntityAttribute_UpdateOptional(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                value = false
            )
        )

        env.replayWithRebuild {
            val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
            assertEquals(false, reloaded.optional)
        }
    }

    @Test
    fun `update attribute optional false to true is persisted`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-optional-true")
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
                description = null
            )
        )

        env.dispatch(
            ModelAction.EntityAttribute_UpdateOptional(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                value = true
            )
        )

        val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
        assertEquals(true, reloaded.optional)
    }

    @Test
    fun `update attribute optional with same value does not create new event`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-optional-noop")
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
                optional = true,
                name = null,
                description = null
            )
        )

        val beforeEventId = env.findLastStoredModelChangeEvent(modelRef).eventId
        env.dispatch(
            ModelAction.EntityAttribute_UpdateOptional(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                value = true
            )
        )
        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        assertEquals(beforeEventId, afterEventId)
    }
}
