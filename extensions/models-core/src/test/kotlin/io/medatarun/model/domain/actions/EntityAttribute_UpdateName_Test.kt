package io.medatarun.model.domain.actions

import io.medatarun.model.actions.ModelAction.EntityAttribute_UpdateName
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.entityAttributeRef
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EntityAttribute_UpdateName_Test {

    @Test
    fun `update attribute name is persisted`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val attr = env.createAttribute(name = null)
        val nextValue = LocalizedTextNotLocalized("New name")
        val attributeRef = entityAttributeRef(attr.key)
        env.runtime.dispatch(
            EntityAttribute_UpdateName(
                env.sampleModelRef,
                env.sampleEntityRef,
                attributeRef,
                nextValue
            )
        )
        val reloaded = env.reloadAttribute(attributeRef)
        assertEquals(nextValue, reloaded.name)
    }

    @Test
    fun `update attribute name to null stays null`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val attr = env.createAttribute(name = LocalizedTextNotLocalized("Name"))
        val attributeRef = entityAttributeRef(attr.key)
        env.runtime.dispatch(
            EntityAttribute_UpdateName(
                env.sampleModelRef,
                env.sampleEntityRef,
                attributeRef,
                null
            )
        )
        val reloaded = env.reloadAttribute(attributeRef)
        assertNull(reloaded.name)
    }
}