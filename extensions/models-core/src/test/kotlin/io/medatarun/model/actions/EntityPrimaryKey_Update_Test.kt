package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
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
        val modelRef = modelRefKey(ModelKey("pk-update-order"))
        val entityRef = EntityRef.ByKey(EntityKey("customer"))
        val stringTypeRef = TypeRef.ByKey(TypeKey("String"))
        val idAttributeRef = EntityAttributeRef.ByKey(AttributeKey("id"))
        val codeAttributeRef = EntityAttributeRef.ByKey(AttributeKey("code"))
        val regionAttributeRef = EntityAttributeRef.ByKey(AttributeKey("region"))
        env.dispatch(
            ModelAction.Model_Create(
                key = modelRef.key,
                name = LocalizedTextNotLocalized("Primary key tests"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = modelRef,
                typeKey = stringTypeRef.key,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = modelRef,
                entityKey = entityRef.key,
                name = null,
                description = null,
                identityAttributeKey = idAttributeRef.key,
                identityAttributeType = stringTypeRef,
                identityAttributeName = null,
                documentationHome = null
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
                attributeRef = listOf(codeAttributeRef, regionAttributeRef),
            )
        )


        env.replayWithRebuild {
            val codeAttribute = env.queries.findEntityAttribute(modelRef, entityRef, codeAttributeRef)
            val regionAttribute = env.queries.findEntityAttribute(modelRef, entityRef, regionAttributeRef)
            val primaryKey: EntityPrimaryKey =
                assertNotNull(env.queries.findEntityPrimaryKeyOptional(modelRef, entityRef))
            assertTrue(primaryKey.containsInOrder(listOf(codeAttribute.id, regionAttribute.id)))
        }
    }

    @Test
    fun `update entity primary key with empty list deletes primary key`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey(ModelKey("pk-delete"))
        val entityRef = EntityRef.ByKey(EntityKey("order"))
        val stringTypeRef = TypeRef.ByKey(TypeKey("String"))
        val idAttributeRef = EntityAttributeRef.ByKey(AttributeKey("id"))
        env.dispatch(
            ModelAction.Model_Create(
                key = modelRef.key,
                name = LocalizedTextNotLocalized("Primary key tests"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = modelRef,
                typeKey = stringTypeRef.key,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = modelRef,
                entityKey = entityRef.key,
                name = null,
                description = null,
                identityAttributeKey = idAttributeRef.key,
                identityAttributeType = stringTypeRef,
                identityAttributeName = null,
                documentationHome = null
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
        val modelRef = modelRefKey(ModelKey("pk-no-op"))
        val entityRef = EntityRef.ByKey(EntityKey("product"))
        val stringTypeRef = TypeRef.ByKey(TypeKey("String"))
        val idAttributeRef = EntityAttributeRef.ByKey(AttributeKey("id"))
        env.dispatch(
            ModelAction.Model_Create(
                key = modelRef.key,
                name = LocalizedTextNotLocalized("Primary key tests"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = modelRef,
                typeKey = stringTypeRef.key,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = modelRef,
                entityKey = entityRef.key,
                name = null,
                description = null,
                identityAttributeKey = idAttributeRef.key,
                identityAttributeType = stringTypeRef,
                identityAttributeName = null,
                documentationHome = null
            )
        )

        val idAttribute = env.queries.findEntityAttribute(modelRef, entityRef, idAttributeRef)
        val beforeEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        env.dispatch(
            ModelAction.EntityPrimaryKey_Update(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = listOf(EntityAttributeRef.ById(idAttribute.id))
            )
        )

        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId
        assertEquals(beforeEventId, afterEventId)
    }
}
