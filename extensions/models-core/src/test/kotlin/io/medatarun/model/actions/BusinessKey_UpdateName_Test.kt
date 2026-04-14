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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class BusinessKey_UpdateName_Test {

    @Test
    fun `update business key name`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-update-name")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("order")
        val idAttributeRef = entityAttributeRefKey("id")
        val businessKeyRef = businessKeyRefKey("order_business_key")
        val newName = LocalizedTextNotLocalized("Updated business key name")

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

        env.dispatch(ModelAction.BusinessKey_Update_Name(modelRef, businessKeyRef, newName))

        env.replayWithRebuild {
            val reloaded = env.queries.findBusinessKey(modelRef, businessKeyRef)
            assertEquals(newName, reloaded.name)
        }
    }

    @Test
    fun `update business key name with null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-update-name-null")
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

        env.dispatch(ModelAction.BusinessKey_Update_Name(modelRef, businessKeyRef, null))

        val reloaded = env.queries.findBusinessKeyOptional(modelRef, businessKeyRef)
        assertNotNull(reloaded)
        assertNull(reloaded.name)
    }

    @Test
    fun `update business key name with same value does not create new event`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-update-name-noop")
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
        env.dispatch(
            ModelAction.BusinessKey_Update_Name(
                modelRef = modelRef,
                businessKeyRef = businessKeyRef,
                value = LocalizedTextNotLocalized("Order business key")
            )
        )
        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        assertEquals(beforeEventId, afterEventId)
    }
}
