package io.medatarun.model.actions

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityAttributeRef
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
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
                EntityAttributeRef.entityAttributeRefKey("bk")
            )
        )
        assertNull(
            env.query.findEntityAttributeOptional(
                env.sampleModelRef,
                env.sampleEntityRef,
                EntityAttributeRef.entityAttributeRefKey("firstname")
            )
        )
        assertNotNull(
            env.query.findEntityAttributeOptional(
                env.sampleModelRef,
                env.sampleEntityRef,
                EntityAttributeRef.entityAttributeRefKey("lastname")
            )
        )

    }

}