package io.medatarun.model.domain.actions

import io.medatarun.model.actions.ModelAction.EntityAttribute_UpdateOptional
import io.medatarun.model.domain.entityAttributeRef
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EntityAttribute_UpdateOptional_Test {


    @Test
    fun `update attribute optional true to false is persisted`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val attr = env.createAttribute(optional = true)
        val nextValue = false
        val attributeRef = entityAttributeRef(attr.key)
        env.runtime.dispatch(
            EntityAttribute_UpdateOptional(env.sampleModelRef, env.sampleEntityRef, attributeRef, nextValue)
        )
        val reloaded = env.reloadAttribute(attributeRef)
        assertEquals(nextValue, reloaded.optional)
    }

    @Test
    fun `update attribute optional false to true is persisted`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val attr = env.createAttribute(optional = false)
        val nextValue = true
        val attributeRef = entityAttributeRef(attr.key)
        env.runtime.dispatch(
            EntityAttribute_UpdateOptional(env.sampleModelRef, env.sampleEntityRef, attributeRef, nextValue)
        )
        val reloaded = env.reloadAttribute(attributeRef)
        assertEquals(nextValue, reloaded.optional)
    }
}