package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

@EnableDatabaseTests
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