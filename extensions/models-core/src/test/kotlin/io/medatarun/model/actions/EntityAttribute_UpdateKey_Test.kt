package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.actions.ModelAction.EntityAttribute_UpdateKey
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.UpdateAttributeDuplicateKeyException
import io.medatarun.model.domain.entityAttributeRef
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@EnableDatabaseTests
class EntityAttribute_UpdateKey_Test {

    @Test
    fun `update attribute key with duplicate key throws exception`() {
        val env = TestEnvEntityAttribute()
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
        val env = TestEnvEntityAttribute()
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
    fun `update attribute key does not loose entity pk attribute`() {

        // Create model with entity and an attribute "id" defined as primary key

        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val e = env.query.findEntity(env.sampleModelRef, env.sampleEntityRef)
        val m = env.query.findModelAggregate(env.sampleModelRef)

        val pk = m.findEntityPrimaryKeyOptional(e.ref)
        assertNotNull(pk)

        val pkAttributeId = pk.participants.firstOrNull()?.attributeId
        assertNotNull(pkAttributeId)

        // Change attribute key
        val attributeNewKey = AttributeKey("id_next")
        val attributeRef = entityAttributeRef(pkAttributeId)
        env.runtime.dispatch(
            EntityAttribute_UpdateKey(
                env.sampleModelRef,
                env.sampleEntityRef,
                attributeRef = attributeRef,
                value = attributeNewKey
            )
        )
        env.reloadAttribute(attributeRef, entityAttributeRef(attributeNewKey))

        // Reload model
        val m2 = env.query.findModelAggregate(env.sampleModelRef)
        val pk2 = assertNotNull(m2.findEntityPrimaryKeyOptional(e.ref))
        val pk2AttributeId = assertNotNull(pk2.participants.firstOrNull()?.attributeId)

        assertEquals(pkAttributeId, pk2AttributeId)




    }

}