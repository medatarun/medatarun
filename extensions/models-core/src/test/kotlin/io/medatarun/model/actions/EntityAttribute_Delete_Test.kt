package io.medatarun.model.actions

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.DeleteAttributeIdentifierException
import io.medatarun.model.domain.EntityAttributeRef
import io.medatarun.model.domain.entityAttributeRef
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EntityAttribute_Delete_Test {


    @Test
    fun `delete entity attribute in model then attribute removed`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        env.createAttribute(attributeKey = AttributeKey("bk"))
        env.createAttribute(attributeKey = AttributeKey("firstname"))
        env.createAttribute(attributeKey = AttributeKey("lastname"))
        env.dispatch(
            ModelAction.EntityAttribute_Delete(
                env.sampleModelRef,
                env.sampleEntityRef,
                EntityAttributeRef.ByKey(AttributeKey("firstname"))
            )
        )

        assertNotNull(
            env.query.findEntityAttributeOptional(
                env.sampleModelRef,
                env.sampleEntityRef,
                entityAttributeRef("bk")
            )
        )
        assertNull(
            env.query.findEntityAttributeOptional(
                env.sampleModelRef,
                env.sampleEntityRef,
                entityAttributeRef("firstname")
            )
        )
        assertNotNull(
            env.query.findEntityAttributeOptional(
                env.sampleModelRef,
                env.sampleEntityRef,
                entityAttributeRef("lastname")
            )
        )


    }

    @Test
    fun `delete entity attribute used as identifier throws error`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        env.createAttribute(attributeKey = AttributeKey("bk"))
        env.createAttribute(attributeKey = AttributeKey("firstname"))
        env.createAttribute(attributeKey = AttributeKey("lastname"))

        val reloaded = env.query.findEntity(env.sampleModelRef, env.sampleEntityRef)
        assertThrows<DeleteAttributeIdentifierException> {
            env.dispatch(
                ModelAction.EntityAttribute_Delete(
                    env.sampleModelRef,
                    env.sampleEntityRef,
                    EntityAttributeRef.ById(reloaded.identifierAttributeId)
                )
            )
        }
    }

}