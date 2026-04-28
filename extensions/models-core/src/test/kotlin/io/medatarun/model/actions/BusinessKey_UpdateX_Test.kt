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

@EnableDatabaseTests
class BusinessKey_UpdateX_Test {

    @Test
    fun `update business key with model not found`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-update-x-model-not-found")
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
            name = LocalizedText("Order business key"),
            description = LocalizedMarkdown("Order business key description")
        )

        assertThrows<ModelNotFoundException> {
            env.dispatch(
                ModelAction.BusinessKey_Update_Name(
                    modelRef = modelRefKey("unknown-model"),
                    businessKeyRef = businessKeyRef,
                    value = null
                )
            )
        }
    }

    @Test
    fun `update business key with business key not found`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("business-key-update-x-bk-not-found")

        env.modelCreate(modelRef.key)

        assertThrows<BusinessKeyNotFoundException> {
            env.dispatch(
                ModelAction.BusinessKey_Update_Name(
                    modelRef = modelRef,
                    businessKeyRef = businessKeyRefKey("unknown-business-key"),
                    value = null
                )
            )
        }
    }
}
