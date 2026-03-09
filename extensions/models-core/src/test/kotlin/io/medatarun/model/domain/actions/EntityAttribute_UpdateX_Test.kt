package io.medatarun.model.domain.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.EntityAttributeNotFoundException
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityNotFoundException
import io.medatarun.model.domain.EntityRef
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelNotFoundException
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.entityAttributeRef
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class EntityAttribute_UpdateX_Test {


    @Test
    fun `update attribute with wrong model id throws ModelNotFoundException`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        env.createAttribute()
        assertFailsWith<ModelNotFoundException> {
            env.dispatch(
                ModelAction.EntityAttribute_UpdateName(
                    modelRef = modelRefKey(ModelKey("unknown")),
                    entityRef = EntityRef.ByKey(EntityKey("unknownEntity")),
                    attributeRef = entityAttributeRef("unknownAttribute"),
                    value = null
                )
            )
        }

    }

    @Test
    fun `update attribute with wrong entity id throws ModelEntityNotFoundException`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        env.createAttribute()
        assertFailsWith<EntityNotFoundException> {
            env.dispatch(
                ModelAction.EntityAttribute_UpdateName(
                    modelRef = env.sampleModelRef,
                    entityRef = EntityRef.ByKey(EntityKey("unknownEntity")),
                    attributeRef = entityAttributeRef("unknownAttribute"),
                    value = null
                )
            )
        }
    }


    @Test
    fun `update attribute with wrong attribute id throws ModelEntityNotFoundException`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        env.createAttribute()
        assertFailsWith<EntityAttributeNotFoundException> {
            env.dispatch(
                ModelAction.EntityAttribute_UpdateName(
                    modelRef = env.sampleModelRef,
                    entityRef = env.sampleEntityRef,
                    attributeRef = entityAttributeRef("unknownAttribute"),
                    value = null
                )
            )
        }
    }

}