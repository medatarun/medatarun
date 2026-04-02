package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.tags.core.domain.TagAttachScopeMismatchException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@EnableDatabaseTests
class Relationship_XTag_Test {

    @Test
    fun `add and delete tag on relationship persists tag ids`() {
        val env = TestEnvEntityUpdate()
        val relationshipKey = RelationshipKey("works-with")
        val relationshipRef = RelationshipRef.ByKey(relationshipKey)
        val globalTag = env.runtime.createGlobalTag("g-rel", "t-rel")

        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = env.modelRef,
                relationshipKey = relationshipKey,
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("a"),
                roleAEntityRef = env.primaryEntityRef,
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("b"),
                roleBEntityRef = env.secondaryEntityRef,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )

        env.dispatch(ModelAction.Relationship_AddTag(env.modelRef, relationshipRef, globalTag.ref))
        assertEquals(listOf(globalTag.id), env.query.findModel(env.modelRef).findRelationship(relationshipRef).tags)

        env.dispatch(ModelAction.Relationship_DeleteTag(env.modelRef, relationshipRef, globalTag.ref))
        env.runtime.replayWithRebuild {
            assertTrue(env.query.findModel(env.modelRef).findRelationship(relationshipRef).tags.isEmpty())
        }
    }

    @Test
    fun `add local tag of same model on relationship persists tag ids`() {
        val env = TestEnvEntityUpdate()
        val relationshipKey = RelationshipKey("works-with")
        val relationshipRef = RelationshipRef.ByKey(relationshipKey)
        val localTag = env.runtime.createLocalTagInModelScope(env.modelRef, "local-rel-tag")

        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = env.modelRef,
                relationshipKey = relationshipKey,
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("a"),
                roleAEntityRef = env.primaryEntityRef,
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("b"),
                roleBEntityRef = env.secondaryEntityRef,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )

        env.dispatch(ModelAction.Relationship_AddTag(env.modelRef, relationshipRef, localTag.ref))
        assertEquals(listOf(localTag.id), env.query.findModel(env.modelRef).findRelationship(relationshipRef).tags)
    }

    @Test
    fun `add local tag of another model on relationship then error`() {
        val env = TestEnvEntityUpdate()
        val relationshipKey = RelationshipKey("works-with")
        val relationshipRef = RelationshipRef.ByKey(relationshipKey)
        val foreignModelRef = modelRefKey("model-rel-2")

        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = env.modelRef,
                relationshipKey = relationshipKey,
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("a"),
                roleAEntityRef = env.primaryEntityRef,
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("b"),
                roleBEntityRef = env.secondaryEntityRef,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        env.runtime.dispatch(
            ModelAction.Model_Create(
                key = ModelKey("model-rel-2"),
                name = LocalizedTextNotLocalized("Model rel 2"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        val foreignTag = env.runtime.createLocalTagInModelScope(foreignModelRef, "foreign-rel-tag")

        assertFailsWith<TagAttachScopeMismatchException> {
            env.dispatch(ModelAction.Relationship_AddTag(env.modelRef, relationshipRef, foreignTag.ref))
        }
    }

    @Test
    fun `add and delete tag on relationship attribute persists tag ids`() {
        val env = TestEnvEntityUpdate()
        val relationshipKey = RelationshipKey("employs")
        val relationshipRef = RelationshipRef.ByKey(relationshipKey)
        val attributeRef = RelationshipAttributeRef.ByKey(AttributeKey("startDate"))
        val globalTag = env.runtime.createGlobalTag("g-ra", "t-ra")

        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = env.modelRef,
                relationshipKey = relationshipKey,
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("employer"),
                roleAEntityRef = env.primaryEntityRef,
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("employee"),
                roleBEntityRef = env.secondaryEntityRef,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = env.modelRef,
                relationshipRef = relationshipRef,
                attributeKey = AttributeKey("startDate"),
                type = typeRef("String"),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.RelationshipAttribute_AddTag(
                env.modelRef,
                relationshipRef,
                attributeRef,
                globalTag.ref
            )
        )
        val added = env.query.findModel(env.modelRef).findRelationshipAttributeOptional(relationshipRef, attributeRef)
        assertNotNull(added)
        assertEquals(listOf(globalTag.id), added.tags)

        env.dispatch(
            ModelAction.RelationshipAttribute_DeleteTag(
                env.modelRef,
                relationshipRef,
                attributeRef,
                globalTag.ref
            )
        )
        val deleted = env.query.findModel(env.modelRef).findRelationshipAttributeOptional(relationshipRef, attributeRef)
        assertNotNull(deleted)
        assertTrue(deleted.tags.isEmpty())
    }

    @Test
    fun `add local tag of same model on relationship attribute persists tag ids`() {
        val env = TestEnvEntityUpdate()
        val relationshipKey = RelationshipKey("employs")
        val relationshipRef = RelationshipRef.ByKey(relationshipKey)
        val attributeRef = RelationshipAttributeRef.ByKey(AttributeKey("startDate"))
        val localTag = env.runtime.createLocalTagInModelScope(env.modelRef, "local-rel-attr-tag")

        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = env.modelRef,
                relationshipKey = relationshipKey,
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("employer"),
                roleAEntityRef = env.primaryEntityRef,
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("employee"),
                roleBEntityRef = env.secondaryEntityRef,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = env.modelRef,
                relationshipRef = relationshipRef,
                attributeKey = AttributeKey("startDate"),
                type = typeRef("String"),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(
            ModelAction.RelationshipAttribute_AddTag(
                env.modelRef,
                relationshipRef,
                attributeRef,
                localTag.ref
            )
        )
        val added = env.query.findModel(env.modelRef).findRelationshipAttributeOptional(relationshipRef, attributeRef)
        assertNotNull(added)
        assertEquals(listOf(localTag.id), added.tags)
    }

    @Test
    fun `add local tag of another model on relationship attribute then error`() {
        val env = TestEnvEntityUpdate()
        val relationshipKey = RelationshipKey("employs")
        val relationshipRef = RelationshipRef.ByKey(relationshipKey)
        val attributeRef = RelationshipAttributeRef.ByKey(AttributeKey("startDate"))
        val foreignModelRef = modelRefKey("model-rel-attr-2")

        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = env.modelRef,
                relationshipKey = relationshipKey,
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("employer"),
                roleAEntityRef = env.primaryEntityRef,
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("employee"),
                roleBEntityRef = env.secondaryEntityRef,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = env.modelRef,
                relationshipRef = relationshipRef,
                attributeKey = AttributeKey("startDate"),
                type = typeRef("String"),
                optional = false,
                name = null,
                description = null
            )
        )
        env.runtime.dispatch(
            ModelAction.Model_Create(
                key = ModelKey("model-rel-attr-2"),
                name = LocalizedTextNotLocalized("Model rel attr 2"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        val foreignTag = env.runtime.createLocalTagInModelScope(foreignModelRef, "foreign-rel-attr-tag")

        assertFailsWith<TagAttachScopeMismatchException> {
            env.dispatch(
                ModelAction.RelationshipAttribute_AddTag(
                    env.modelRef,
                    relationshipRef,
                    attributeRef,
                    foreignTag.ref
                )
            )
        }
    }
}
