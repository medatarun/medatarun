package io.medatarun.model.actions

import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class EntityAttribute_Delete_Test {

    @Test
    fun `delete entity attribute in model then attribute removed`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-delete")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val bkRef = entityAttributeRefKey("bk")
        val firstNameRef = entityAttributeRefKey("firstname")
        val lastNameRef = entityAttributeRefKey("lastname")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate2(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = bkRef.key,
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
            ModelAction.EntityAttribute_Delete(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = firstNameRef
            )
        )

        env.replayWithRebuild {
            assertNotNull(env.queries.findEntityAttributeOptional(modelRef, entityRef, bkRef))
            assertNull(env.queries.findEntityAttributeOptional(modelRef, entityRef, firstNameRef))
            assertNotNull(env.queries.findEntityAttributeOptional(modelRef, entityRef, lastNameRef))
        }
    }
}
