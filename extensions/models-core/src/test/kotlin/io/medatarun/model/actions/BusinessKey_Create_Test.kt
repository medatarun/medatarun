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
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@EnableDatabaseTests
class BusinessKey_Create_Test {

    @Test
    fun `create business key`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-create")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("order")
        val idAttributeRef = entityAttributeRefKey("id")
        val codeAttributeRef = entityAttributeRefKey("code")
        val businessKeyRef = businessKeyRefKey("order_business_key")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.entityAttributeCreate(modelRef, entityRef, idAttributeRef.key, typeRef)
        env.entityAttributeCreate(modelRef, entityRef, codeAttributeRef.key, typeRef)
        env.businessKeyCreate(
            modelRef = modelRef,
            key = businessKeyRef.key,
            entityRef = entityRef,
            participants = listOf(idAttributeRef, codeAttributeRef),
            name = TextSingleLine("Order business key"),
            description = TextMarkdown("Identifies an order in business flows")
        )

        env.replayWithRebuild {
            val businessKey = env.queries.findBusinessKey(modelRef, businessKeyRef)
            val entity = env.queries.findEntity(modelRef, entityRef)
            val idAttribute = env.queries.findEntityAttribute(modelRef, entityRef, idAttributeRef)
            val codeAttribute = env.queries.findEntityAttribute(modelRef, entityRef, codeAttributeRef)
            assertEquals(businessKeyRef.key, businessKey.key)
            assertEquals(entity.id, businessKey.entityId)
            assertEquals(TextSingleLine("Order business key"), businessKey.name)
            assertEquals(TextMarkdown("Identifies an order in business flows"), businessKey.description)
            assertEquals(listOf(idAttribute.id, codeAttribute.id), businessKey.participants.map { it.attributeId })
            assertEquals(listOf(0, 1), businessKey.participants.map { it.position })
        }
    }

    @Test
    fun `create business key on unknown model throw ModelNotFoundException`() {
        val env = ModelTestEnv()
        val entityRef = entityRefKey("order")
        val idAttributeRef = entityAttributeRefKey("id")
        val businessKeyRef = businessKeyRefKey("order_business_key")

        assertThrows<ModelNotFoundException> {
            env.businessKeyCreate(
                modelRef = modelRefKey("unknown-model"),
                key = businessKeyRef.key,
                entityRef = entityRef,
                participants = listOf(idAttributeRef)
            )
        }
    }

    @Test
    fun `create business key on unknown entity throw EntityNotFoundException`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-create-unknown-entity")
        val businessKeyRef = businessKeyRefKey("order_business_key")
        val idAttributeRef = entityAttributeRefKey("id")

        env.modelCreate(modelRef.key)

        assertThrows<EntityNotFoundException> {
            env.businessKeyCreate(
                modelRef = modelRef,
                key = businessKeyRef.key,
                entityRef = entityRefKey("unknown-entity"),
                participants = listOf(idAttributeRef)
            )
        }
    }

    @Test
    fun `create business key with unknown participant throw EntityAttributeNotFoundException`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-create-unknown-attribute")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("order")
        val businessKeyRef = businessKeyRefKey("order_business_key")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)

        assertThrows<EntityAttributeNotFoundException> {
            env.businessKeyCreate(
                modelRef = modelRef,
                key = businessKeyRef.key,
                entityRef = entityRef,
                participants = listOf(entityAttributeRefKey("unknown-attribute"))
            )
        }
    }

    @Test
    fun `create business key with duplicate key throws BusinessKeyCreateDuplicateKeyException`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-create-duplicate-key")
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
            participants = listOf(idAttributeRef)
        )

        assertThrows<BusinessKeyCreateDuplicateKeyException> {
            env.businessKeyCreate(
                modelRef = modelRef,
                key = businessKeyRef.key,
                entityRef = entityRef,
                participants = listOf(idAttributeRef)
            )
        }
        val reloaded = env.queries.findBusinessKeyOptional(modelRef, businessKeyRef)
        assertNotNull(reloaded)
    }
}
