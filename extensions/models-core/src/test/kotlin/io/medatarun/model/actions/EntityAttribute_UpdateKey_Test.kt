package io.medatarun.model.actions

import io.medatarun.model.actions.ModelAction.EntityAttribute_UpdateKey
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.UpdateAttributeDuplicateKeyException
import io.medatarun.model.domain.entityAttributeRef
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EntityAttribute_UpdateKey_Test {

    @Test
    fun `update attribute key with duplicate key throws exception`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvEntityAttribute()
        env.addSampleEntity()
        env.createAttribute(attributeKey = AttributeKey("lastname"))
        env.createAttribute(attributeKey = AttributeKey("firstname"))
        assertFailsWith<UpdateAttributeDuplicateKeyException> {
            // Rename firstname to lastname causes exception because lastname already exists
            val attributeRef = entityAttributeRef("firstname")
            env.runtime.dispatch(
                EntityAttribute_UpdateKey(
                    env.sampleModelRef,
                    env.sampleEntityRef,
                    attributeRef = attributeRef,
                    value = AttributeKey("lastname")
                )
            )
            env.reloadAttribute(
                attributeRef, entityAttributeRef("firstname")
            )
        }
    }

    @Test
    fun `update attribute key with correct key works`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvEntityAttribute()
        env.addSampleEntity()
        env.createAttribute(AttributeKey("lastname"))
        env.createAttribute(AttributeKey("firstname"))
        val attributeRef = entityAttributeRef("firstname")
        env.runtime.dispatch(
            EntityAttribute_UpdateKey(
                env.sampleModelRef,
                env.sampleEntityRef,
                attributeRef,
                AttributeKey("nextname")
            )
        )
        val reloaded = env.reloadAttribute(
            attributeRef, entityAttributeRef("nextname")
        )
        assertEquals(AttributeKey("nextname"), reloaded.key)
    }

    @Test
    fun `update attribute key does not loose entity identifier attribute`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvEntityAttribute()
        env.addSampleEntity()
        val e = env.query.findEntity(env.sampleModelRef, env.sampleEntityRef)
        val attrId = e.identifierAttributeId
        val newKey = AttributeKey("id_next")
        // Be careful to specify "reloadId" because the attribute's id changed
        val attributeRef = entityAttributeRef(attrId)
        env.runtime.dispatch(
            EntityAttribute_UpdateKey(
                env.sampleModelRef,
                env.sampleEntityRef,
                attributeRef = attributeRef,
                value = newKey
            )
        )
        env.reloadAttribute(
            attributeRef, entityAttributeRef(newKey)
        )
        val reloadedEntity = env.query.findEntity(env.sampleModelRef, env.sampleEntityRef)
        assertEquals(attrId, reloadedEntity.identifierAttributeId)


    }

}