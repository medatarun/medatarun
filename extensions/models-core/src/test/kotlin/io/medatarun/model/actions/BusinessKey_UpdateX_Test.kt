package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.domain.BusinessKeyRef.Companion.businessKeyRefKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
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
        val typeRef = TypeRef.typeRefKey("String")
        val entityRef = EntityRef.entityRefKey("order")
        val idAttributeRef = EntityAttributeRef.entityAttributeRefKey("id")
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

        env.dispatch(ModelAction.Model_Create(modelRef.key, LocalizedTextNotLocalized("Model"), null, ModelVersion("1.0.0")))

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
