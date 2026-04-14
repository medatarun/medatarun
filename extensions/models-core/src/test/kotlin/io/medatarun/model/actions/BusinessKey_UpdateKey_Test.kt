package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.domain.BusinessKeyRef.Companion.businessKeyRefKey
import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class BusinessKey_UpdateKey_Test {

    @Test
    fun `update business key key`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-update-key")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("order")
        val idAttributeRef = entityAttributeRefKey("id")
        val businessKeyRef = businessKeyRefKey("order_business_key")
        val updatedBusinessKeyRef = businessKeyRefKey("order_business_key_updated")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.entityAttributeCreate(modelRef, entityRef, idAttributeRef.key, typeRef)
        env.businessKeyCreate(
            modelRef = modelRef,
            key = businessKeyRef.key,
            entityRef = entityRef,
            participants = listOf(idAttributeRef),
            name = LocalizedTextNotLocalized("Order business key"),
            description = LocalizedMarkdownNotLocalized("Order business key description")
        )

        env.dispatch(ModelAction.BusinessKey_Update_Key(modelRef, businessKeyRef, updatedBusinessKeyRef.key))

        env.replayWithRebuild {
            val reloaded = env.queries.findBusinessKey(modelRef, updatedBusinessKeyRef)
            assertEquals(updatedBusinessKeyRef.key, reloaded.key)
            assertNull(env.queries.findBusinessKeyOptional(modelRef, businessKeyRef))
        }
    }

    @Test
    fun `update business key key with duplicate key throws BusinessKeyUpdateDuplicateKeyException`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-update-key-duplicate")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("order")
        val idAttributeRef = entityAttributeRefKey("id")
        val businessKeyRef = businessKeyRefKey("order_business_key")
        val otherBusinessKeyRef = businessKeyRefKey("order_business_key_2")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef = modelRef, typeKey = typeRef.key)
        env.entityCreate(modelRef = modelRef, entityKey = entityRef.key)
        env.entityAttributeCreate(modelRef = modelRef, entityRef = entityRef, attributeKey = idAttributeRef.key, type = typeRef)
        env.businessKeyCreate(
            modelRef = modelRef,
            key = businessKeyRef.key,
            entityRef = entityRef,
            participants = listOf(idAttributeRef),
            name = LocalizedTextNotLocalized("Order business key"),
            description = LocalizedMarkdownNotLocalized("Order business key description")
        )
        env.businessKeyCreate(
            modelRef = modelRef,
            key = otherBusinessKeyRef.key,
            entityRef = entityRef,
            participants = listOf(idAttributeRef),
            name = LocalizedTextNotLocalized("Order business key 2"),
            description = LocalizedMarkdownNotLocalized("Order business key 2 description")
        )

        assertFailsWith<BusinessKeyUpdateDuplicateKeyException> {
            env.dispatch(
                ModelAction.BusinessKey_Update_Key(
                    modelRef = modelRef,
                    businessKeyRef = businessKeyRef,
                    value = otherBusinessKeyRef.key
                )
            )
        }
    }

    @Test
    fun `update business key key with same value does not create new event`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-update-key-noop")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("order")
        val idAttributeRef = entityAttributeRefKey("id")
        val businessKeyRef = businessKeyRefKey("order_business_key")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.entityAttributeCreate(modelRef, entityRef, idAttributeRef.key, typeRef)
        env.businessKeyCreate(
            modelRef = modelRef,
            key = businessKeyRef.key,
            entityRef = entityRef,
            participants = listOf(idAttributeRef),
            name = LocalizedTextNotLocalized("Order business key"),
            description = LocalizedMarkdownNotLocalized("Order business key description")
        )

        val beforeEventId = env.findLastStoredModelChangeEvent(modelRef).eventId
        env.dispatch(ModelAction.BusinessKey_Update_Key(modelRef, businessKeyRef, businessKeyRef.key))
        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        assertEquals(beforeEventId, afterEventId)
    }

    @Test
    fun `update business key key keeps business key accessible`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-update-key-access")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("order")
        val idAttributeRef = entityAttributeRefKey("id")
        val businessKeyRef = businessKeyRefKey("order_business_key")
        val updatedBusinessKeyRef = businessKeyRefKey("order_business_key_alias")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.entityAttributeCreate(modelRef, entityRef, idAttributeRef.key, typeRef)
        env.businessKeyCreate(
            modelRef = modelRef,
            key = businessKeyRef.key,
            entityRef = entityRef,
            participants = listOf(idAttributeRef),
            name = LocalizedTextNotLocalized("Order business key"),
            description = LocalizedMarkdownNotLocalized("Order business key description")
        )

        env.dispatch(ModelAction.BusinessKey_Update_Key(modelRef, businessKeyRef, updatedBusinessKeyRef.key))

        val reloaded = env.queries.findBusinessKeyOptional(modelRef, updatedBusinessKeyRef)
        assertNotNull(reloaded)
    }
}
