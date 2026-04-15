package io.medatarun.model.actions

import io.medatarun.model.domain.EntityAttributeNotFoundException
import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityNotFoundException
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.ModelNotFoundException
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

@EnableDatabaseTests
class EntityAttribute_UpdateX_Test {

    @Test
    fun `update attribute with wrong model id throws ModelNotFoundException`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-x-model")
        val wrongModelRef = modelRefKey("unknown")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("myattribute")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeRef,
                optional = false,
                name = null,
                description = null
            )
        )

        assertFailsWith<ModelNotFoundException> {
            env.dispatch(
                ModelAction.EntityAttribute_UpdateName(
                    modelRef = wrongModelRef,
                    entityRef = entityRef,
                    attributeRef = attributeRef,
                    value = null
                )
            )
        }
    }

    @Test
    fun `update attribute with wrong entity id throws ModelEntityNotFoundException`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-x-entity")
        val entityRef = entityRefKey("entity-a")
        val unknownEntityRef = entityRefKey("unknownEntity")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("myattribute")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeRef,
                optional = false,
                name = null,
                description = null
            )
        )

        assertFailsWith<EntityNotFoundException> {
            env.dispatch(
                ModelAction.EntityAttribute_UpdateName(
                    modelRef = modelRef,
                    entityRef = unknownEntityRef,
                    attributeRef = attributeRef,
                    value = null
                )
            )
        }
    }

    @Test
    fun `update attribute with wrong attribute id throws ModelEntityNotFoundException`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-update-x-attribute")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("myattribute")
        val unknownAttributeRef = entityAttributeRefKey("unknownAttribute")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeRef,
                optional = false,
                name = null,
                description = null
            )
        )

        assertFailsWith<EntityAttributeNotFoundException> {
            env.dispatch(
                ModelAction.EntityAttribute_UpdateName(
                    modelRef = modelRef,
                    entityRef = entityRef,
                    attributeRef = unknownAttributeRef,
                    value = null
                )
            )
        }
    }
}
