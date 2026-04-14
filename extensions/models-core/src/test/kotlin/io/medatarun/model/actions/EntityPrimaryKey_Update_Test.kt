package io.medatarun.model.actions

import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityPrimaryKey
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@EnableDatabaseTests
class EntityPrimaryKey_Update_Test {

    @Test
    fun `update entity primary key with multiple attributes keeps order`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("pk-update-order")
        val entityRef = entityRefKey("customer")
        val stringTypeRef = typeRefKey("String")
        val idAttributeRef = entityAttributeRefKey("id")
        val codeAttributeRef = entityAttributeRefKey("code")
        val regionAttributeRef = entityAttributeRefKey("region")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, stringTypeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = idAttributeRef.key,
                type = stringTypeRef,
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = codeAttributeRef.key,
                type = stringTypeRef,
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = regionAttributeRef.key,
                type = stringTypeRef,
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.EntityPrimaryKey_Update(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = listOf(codeAttributeRef, regionAttributeRef)
            )
        )

        env.replayWithRebuild {
            val codeAttribute = env.queries.findEntityAttribute(modelRef, entityRef, codeAttributeRef)
            val regionAttribute = env.queries.findEntityAttribute(modelRef, entityRef, regionAttributeRef)
            val primaryKey: EntityPrimaryKey = assertNotNull(env.queries.findEntityPrimaryKeyOptional(modelRef, entityRef))
            assertTrue(primaryKey.containsInOrder(listOf(codeAttribute.id, regionAttribute.id)))
        }
    }

    @Test
    fun `update entity primary key with empty list deletes primary key`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("pk-delete")
        val entityRef = entityRefKey("order")
        val stringTypeRef = typeRefKey("String")
        val idAttributeRef = entityAttributeRefKey("id")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, stringTypeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = idAttributeRef.key,
                type = stringTypeRef,
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

        env.dispatch(
            ModelAction.EntityPrimaryKey_Update(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = emptyList()
            )
        )

        env.replayWithRebuild {
            assertNull(env.queries.findEntityPrimaryKeyOptional(modelRef, entityRef))
        }
    }

    @Test
    fun `update entity primary key with same participants does not create new event`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("pk-no-op")
        val entityRef = entityRefKey("product")
        val stringTypeRef = typeRefKey("String")
        val idAttributeRef = entityAttributeRefKey("id")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, stringTypeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = idAttributeRef.key,
                type = stringTypeRef,
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

        val beforeEventId = env.findLastStoredModelChangeEvent(modelRef).eventId
        env.dispatch(
            ModelAction.EntityPrimaryKey_Update(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = listOf(idAttributeRef)
            )
        )
        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        assertEquals(beforeEventId, afterEventId)
    }
}
