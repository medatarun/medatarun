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
class BusinessKey_UpdateDescription_Test {

    @Test
    fun `update business key description`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-update-description")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("order")
        val idAttributeRef = entityAttributeRefKey("id")
        val businessKeyRef = businessKeyRefKey("order_business_key")
        val newDescription = LocalizedMarkdownNotLocalized("Updated business key description")

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
                name = LocalizedTextNotLocalized("Order business key"),
                key = businessKeyRef.key,
                description = LocalizedMarkdownNotLocalized("Order business key description"),
                entityRef = entityRef,
                participants = listOf(idAttributeRef)
            )
        )

        env.dispatch(ModelAction.BusinessKey_Update_Description(modelRef, businessKeyRef, newDescription))

        env.replayWithRebuild {
            val reloaded = env.queries.findBusinessKey(modelRef, businessKeyRef)
            assertEquals(newDescription, reloaded.description)
        }
    }

    @Test
    fun `update business key description with null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-update-description-null")
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
                name = LocalizedTextNotLocalized("Order business key"),
                key = businessKeyRef.key,
                description = LocalizedMarkdownNotLocalized("Order business key description"),
                entityRef = entityRef,
                participants = listOf(idAttributeRef)
            )
        )

        env.dispatch(ModelAction.BusinessKey_Update_Description(modelRef, businessKeyRef, null))

        val reloaded = env.queries.findBusinessKeyOptional(modelRef, businessKeyRef)
        assertNotNull(reloaded)
        assertNull(reloaded.description)
    }

    @Test
    fun `update business key description with same value does not create new event`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-update-description-noop")
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
                name = LocalizedTextNotLocalized("Order business key"),
                key = businessKeyRef.key,
                description = LocalizedMarkdownNotLocalized("Order business key description"),
                entityRef = entityRef,
                participants = listOf(idAttributeRef)
            )
        )

        val beforeEventId = env.findLastStoredModelChangeEvent(modelRef).eventId
        env.dispatch(
            ModelAction.BusinessKey_Update_Description(
                modelRef = modelRef,
                businessKeyRef = businessKeyRef,
                value = LocalizedMarkdownNotLocalized("Order business key description")
            )
        )
        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        assertEquals(beforeEventId, afterEventId)
    }
}
