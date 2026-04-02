package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.tags.core.domain.TagAttachScopeMismatchException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@EnableDatabaseTests
class EntityAttribute_XTag_Test {

    @Test
    fun `add and delete tag on entity attribute persists tag ids`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val attribute = env.createAttribute(attributeKey = AttributeKey("tagged"))
        val globalTag = env.runtime.createGlobalTag("g-ea", "t-ea")

        env.dispatch(
            ModelAction.EntityAttribute_AddTag(
                env.sampleModelRef,
                env.sampleEntityRef,
                EntityAttributeRef.ById(attribute.id),
                globalTag.ref
            )
        )
        val added = env.query.findEntityAttribute(
            env.sampleModelRef,
            env.sampleEntityRef,
            EntityAttributeRef.ById(attribute.id)
        )
        assertEquals(listOf(globalTag.id), added.tags)

        env.dispatch(
            ModelAction.EntityAttribute_DeleteTag(
                env.sampleModelRef,
                env.sampleEntityRef,
                EntityAttributeRef.ById(attribute.id),
                globalTag.ref
            )
        )
        val deleted = env.query.findEntityAttribute(
            env.sampleModelRef,
            env.sampleEntityRef,
            EntityAttributeRef.ById(attribute.id)
        )
        assertTrue(deleted.tags.isEmpty())
    }

    @Test
    fun `add local tag of same model on entity attribute persists tag ids`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val attribute = env.createAttribute(attributeKey = AttributeKey("tagged"))
        val localTag = env.runtime.createLocalTagInModelScope(env.sampleModelRef, "local-ea-tag")

        env.dispatch(
            ModelAction.EntityAttribute_AddTag(
                env.sampleModelRef,
                env.sampleEntityRef,
                EntityAttributeRef.ById(attribute.id),
                localTag.ref
            )
        )
        val added = env.query.findEntityAttribute(
            env.sampleModelRef,
            env.sampleEntityRef,
            EntityAttributeRef.ById(attribute.id)
        )
        assertEquals(listOf(localTag.id), added.tags)
    }

    @Test
    fun `add local tag of another model on entity attribute then error`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val attribute = env.createAttribute(attributeKey = AttributeKey("tagged"))
        val foreignModelRef = modelRefKey("sample-model-2")
        env.runtime.dispatch(
            ModelAction.Model_Create(
                key = ModelKey("sample-model-2"),
                name = LocalizedTextNotLocalized("Sample model 2"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        val foreignTag = env.runtime.createLocalTagInModelScope(foreignModelRef, "foreign-ea-tag")

        assertFailsWith<TagAttachScopeMismatchException> {
            env.dispatch(
                ModelAction.EntityAttribute_AddTag(
                    env.sampleModelRef,
                    env.sampleEntityRef,
                    EntityAttributeRef.ById(attribute.id),
                    foreignTag.ref
                )
            )
        }
    }
}