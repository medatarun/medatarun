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

        env.dispatch(ModelAction.Model_Create(modelRef.key, LocalizedTextNotLocalized("Model"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(modelRef, typeRef.key, null, null))
        env.dispatch(ModelAction.Entity_Create2(modelRef, entityRef.key, null, null, null))
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                name = null,
                attributeKey = idAttributeRef.key,
                type = typeRef,
                optional = false,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                name = null,
                attributeKey = codeAttributeRef.key,
                type = typeRef,
                optional = false,
                description = null
            )
        )
        env.dispatch(
            ModelAction.BusinessKey_Create(
                modelRef = modelRef,
                name = LocalizedTextNotLocalized("Order business key"),
                key = businessKeyRef.key,
                description = LocalizedMarkdownNotLocalized("Identifies an order in business flows"),
                entityRef = entityRef,
                participants = listOf(idAttributeRef, codeAttributeRef)
            )
        )

        env.replayWithRebuild {
            val businessKey = env.queries.findBusinessKey(modelRef, businessKeyRef)
            val entity = env.queries.findEntity(modelRef, entityRef)
            val idAttribute = env.queries.findEntityAttribute(modelRef, entityRef, idAttributeRef)
            val codeAttribute = env.queries.findEntityAttribute(modelRef, entityRef, codeAttributeRef)
            assertEquals(businessKeyRef.key, businessKey.key)
            assertEquals(entity.id, businessKey.entityId)
            assertEquals(LocalizedTextNotLocalized("Order business key"), businessKey.name)
            assertEquals(LocalizedMarkdownNotLocalized("Identifies an order in business flows"), businessKey.description)
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
            env.dispatch(
                ModelAction.BusinessKey_Create(
                    modelRef = modelRefKey("unknown-model"),
                    name = null,
                    key = businessKeyRef.key,
                    description = null,
                    entityRef = entityRef,
                    participants = listOf(idAttributeRef)
                )
            )
        }
    }

    @Test
    fun `create business key on unknown entity throw EntityNotFoundException`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-create-unknown-entity")
        val businessKeyRef = businessKeyRefKey("order_business_key")
        val idAttributeRef = entityAttributeRefKey("id")

        env.dispatch(ModelAction.Model_Create(modelRef.key, LocalizedTextNotLocalized("Model"), null, ModelVersion("1.0.0")))

        assertThrows<EntityNotFoundException> {
            env.dispatch(
                ModelAction.BusinessKey_Create(
                    modelRef = modelRef,
                    name = null,
                    key = businessKeyRef.key,
                    description = null,
                    entityRef = entityRefKey("unknown-entity"),
                    participants = listOf(idAttributeRef)
                )
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

        env.dispatch(ModelAction.Model_Create(modelRef.key, LocalizedTextNotLocalized("Model"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(modelRef, typeRef.key, null, null))
        env.dispatch(ModelAction.Entity_Create2(modelRef, entityRef.key, null, null, null))

        assertThrows<EntityAttributeNotFoundException> {
            env.dispatch(
                ModelAction.BusinessKey_Create(
                    modelRef = modelRef,
                    name = null,
                    key = businessKeyRef.key,
                    description = null,
                    entityRef = entityRef,
                    participants = listOf(entityAttributeRefKey("unknown-attribute"))
                )
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

        env.dispatch(ModelAction.Model_Create(modelRef.key, LocalizedTextNotLocalized("Model"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(modelRef, typeRef.key, null, null))
        env.dispatch(ModelAction.Entity_Create2(modelRef, entityRef.key, null, null, null))
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                name = null,
                attributeKey = idAttributeRef.key,
                type = typeRef,
                optional = false,
                description = null
            )
        )
        env.dispatch(
            ModelAction.BusinessKey_Create(
                modelRef = modelRef,
                name = null,
                key = businessKeyRef.key,
                description = null,
                entityRef = entityRef,
                participants = listOf(idAttributeRef)
            )
        )

        assertThrows<BusinessKeyCreateDuplicateKeyException> {
            env.dispatch(
                ModelAction.BusinessKey_Create(
                    modelRef = modelRef,
                    name = null,
                    key = businessKeyRef.key,
                    description = null,
                    entityRef = entityRef,
                    participants = listOf(idAttributeRef)
                )
            )
        }
        val reloaded = env.queries.findBusinessKeyOptional(modelRef, businessKeyRef)
        assertNotNull(reloaded)
    }
}
