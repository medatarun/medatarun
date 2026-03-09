package io.medatarun.model.domain.actions

import io.medatarun.model.actions.ModelAction.EntityAttribute_UpdateDescription
import io.medatarun.model.domain.LocalizedMarkdownNotLocalized
import io.medatarun.model.domain.entityAttributeRef
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EntityAttribute_UpdateDescription_Test {



    @Test
    fun `update attribute description is persisted`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val attr = env.createAttribute(description = null)
        val nextValue = LocalizedMarkdownNotLocalized("New description")
        val attributeRef = entityAttributeRef(attr.key)
        env.runtime.dispatch(
            EntityAttribute_UpdateDescription(env.sampleModelRef, env.sampleEntityRef, attributeRef, nextValue)
        )
        val reloaded = env.reloadAttribute(attributeRef)
        assertEquals(nextValue, reloaded.description)
    }

    @Test
    fun `update attribute description to null stays null`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val attr = env.createAttribute(description = LocalizedMarkdownNotLocalized("New description"))
        val attributeRef = entityAttributeRef(attr.key)
        env.runtime.dispatch(
            EntityAttribute_UpdateDescription(env.sampleModelRef, env.sampleEntityRef, attributeRef, null)
        )
        val reloaded = env.reloadAttribute(attributeRef)
        assertNull(reloaded.description)
    }

}