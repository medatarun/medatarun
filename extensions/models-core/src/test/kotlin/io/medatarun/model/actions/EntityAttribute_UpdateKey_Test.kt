package io.medatarun.model.actions

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefId
import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.TextSingleLine
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.UpdateAttributeDuplicateKeyException
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@EnableDatabaseTests
class EntityAttribute_UpdateKey_Test {

    @Test
    fun `update attribute key with duplicate key throws exception`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-key-duplicate")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val lastNameRef = entityAttributeRefKey("lastname")
        val firstNameRef = entityAttributeRefKey("firstname")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = lastNameRef.key,
                type = typeRef,
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = firstNameRef.key,
                type = typeRef,
                optional = false,
                name = null,
                description = null
            )
        )

        assertFailsWith<UpdateAttributeDuplicateKeyException> {
            env.dispatch(
                ModelAction.EntityAttribute_UpdateKey(
                    modelRef = modelRef,
                    entityRef = entityRef,
                    attributeRef = firstNameRef,
                    value = lastNameRef.key
                )
            )
        }
    }

    @Test
    fun `update attribute key with correct key works`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-key")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val firstNameRef = entityAttributeRefKey("firstname")
        val nextNameRef = entityAttributeRefKey("nextname")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = entityAttributeRefKey("lastname").key,
                type = typeRef,
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = firstNameRef.key,
                type = typeRef,
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.EntityAttribute_UpdateKey(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = firstNameRef,
                value = nextNameRef.key
            )
        )

        env.replayWithRebuild {
            val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, nextNameRef)
            assertEquals(nextNameRef.key, reloaded.key)
        }
    }

    @Test
    fun `update attribute key does not loose entity pk attribute`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-key-pk")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val idAttributeRef = entityAttributeRefKey("id")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key, TextSingleLine("Entity primary"))
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = idAttributeRef.key,
                type = typeRef,
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityPrimaryKey_Update(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = listOf(idAttributeRef)
            )
        )

        val beforePrimaryKey = assertNotNull(env.queries.findEntityPrimaryKeyOptional(modelRef, entityRef))
        val beforeAttributeId = assertNotNull(beforePrimaryKey.participants.firstOrNull()?.attributeId)
        val attributeByIdRef = entityAttributeRefId(beforeAttributeId)
        val attributeNewKey = AttributeKey("id_next")

        env.dispatch(
            ModelAction.EntityAttribute_UpdateKey(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeByIdRef,
                value = attributeNewKey
            )
        )

        val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, entityAttributeRefKey(attributeNewKey))
        val afterPrimaryKey = assertNotNull(env.queries.findEntityPrimaryKeyOptional(modelRef, entityRef))
        val afterAttributeId = assertNotNull(afterPrimaryKey.participants.firstOrNull()?.attributeId)

        assertEquals(attributeNewKey, reloaded.key)
        assertEquals(beforeAttributeId, afterAttributeId)
    }

    @Test
    fun `update attribute key with same value does not create new event`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-key-noop")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("firstname")

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

        val beforeEventId = env.findLastStoredModelChangeEvent(modelRef).eventId
        env.dispatch(
            ModelAction.EntityAttribute_UpdateKey(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = attributeRef,
                value = attributeRef.key
            )
        )
        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        assertEquals(beforeEventId, afterEventId)
    }
}
