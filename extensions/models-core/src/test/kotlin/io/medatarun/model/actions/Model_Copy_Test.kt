package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.domain.EntityRef.Companion.entityRefId
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.RelationshipRef.Companion.relationshipRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefId
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.tags.core.domain.TagRef
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.*

@EnableDatabaseTests
class Model_Copy_Test {

    // ------------------------------------------------------------------------
    // Core business behavior
    // ------------------------------------------------------------------------

    @Test
    fun `copy model creates an independent model with the requested key`() {
        val env = ModelTestEnv()
        val sourceRef = modelRefKey("copy-source-core")
        val copiedRef = modelRefKey("copy-target-core")
        val sourceName = LocalizedText("Source model")

        val version = ModelVersion("1.2.3")
        env.dispatch(
            ModelAction.Model_Create(
                key = sourceRef.key,
                name = sourceName,
                description = LocalizedMarkdown("Source description"),
                version = version
            )
        )

        env.dispatch(
            ModelAction.Model_Copy(
                modelRef = sourceRef,
                modelNewKey = copiedRef.key
            )
        )

        val source = env.queries.findModelRoot(sourceRef)
        val copied = env.queries.findModelRoot(copiedRef)
        assertEquals(copiedRef.key, copied.key)
        assertEquals(sourceName, copied.name)
        assertNotEquals(source.id, copied.id)

        // Creating a model immediately creates a first version
        env.assertUniqueVersion(version, copied.id)
    }

    @Test
    fun `copy model does not modify the source model`() {
        val env = ModelTestEnv()
        val sourceRef = modelRefKey("copy-source-unchanged")
        val copiedRef = modelRefKey("copy-target-unchanged")

        env.dispatch(
            ModelAction.Model_Create(
                key = sourceRef.key,
                name = LocalizedText("Source model"),
                description = LocalizedMarkdown("Before copy"),
                version = ModelVersion("2.0.0")
            )
        )

        // Legitimate use of aggregate because we need to test everything it contains
        val sourceBefore = env.queries.findModelAggregate(sourceRef)

        env.dispatch(
            ModelAction.Model_Copy(
                modelRef = sourceRef,
                modelNewKey = copiedRef.key
            )
        )

        // Legitimate use of aggregate because we need to test everything it contains
        val sourceAfter = env.queries.findModelAggregate(sourceRef)
        assertEquals(sourceBefore.id, sourceAfter.id)
        assertEquals(sourceBefore.key, sourceAfter.key)
        assertEquals(sourceBefore.name, sourceAfter.name)
        assertEquals(sourceBefore.description, sourceAfter.description)
        assertEquals(sourceBefore.version, sourceAfter.version)
        assertEquals(sourceBefore.origin, sourceAfter.origin)
        assertEquals(sourceBefore.documentationHome, sourceAfter.documentationHome)
        assertEquals(sourceBefore.types, sourceAfter.types)
        assertEquals(sourceBefore.entities, sourceAfter.entities)
        assertEquals(sourceBefore.relationships, sourceAfter.relationships)
        assertEquals(sourceBefore.attributes, sourceAfter.attributes)
        assertEquals(sourceBefore.tags, sourceAfter.tags)
    }

    @Test
    fun `copy model assigns new ids to copied model, types, entities, relationships, roles and attributes`() {
        val env = ModelTestEnv()

        val sourceRef = modelRefKey("copy-source-ids")
        val copiedRef = modelRefKey("copy-target-ids")

        val customerRef = EntityRef.entityRefKey("customer")
        val orderRef = EntityRef.entityRefKey("order")
        val relationshipRef = relationshipRefKey("places")

        val typeStringRef = typeRefKey("String")
        val typeBooleanRef = typeRefKey("Boolean")

        env.dispatch(
            ModelAction.Model_Create(
                sourceRef.key,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, typeStringRef.key, null, null))
        env.dispatch(ModelAction.Type_Create(sourceRef, typeBooleanRef.key, null, null))
        env.dispatch(
            ModelAction.Entity_Create(
                sourceRef,
                EntityKey("customer"),
                LocalizedText("Customer"),
                null,
                null
            )
        )
        env.dispatch(
            ModelAction.Entity_Create(
                sourceRef,
                EntityKey("order"),
                LocalizedText("Order"),
                null,
                null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = sourceRef,
                entityRef = customerRef,
                attributeKey = AttributeKey("vip"),
                type = typeBooleanRef,
                optional = false,
                name = LocalizedText("VIP"),
                description = null
            )
        )
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = sourceRef,
                relationshipKey = RelationshipKey("places"),
                name = LocalizedText("Places"),
                description = null,
                roleAKey = RelationshipRoleKey("buyer"),
                roleAEntityRef = customerRef,
                roleAName = LocalizedText("Buyer"),
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("purchase"),
                roleBEntityRef = orderRef,
                roleBName = LocalizedText("Purchase"),
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = sourceRef,
                relationshipRef = relationshipRef,
                attributeKey = AttributeKey("confirmed"),
                type = typeBooleanRef,
                optional = false,
                name = LocalizedText("Confirmed"),
                description = null
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedRef.key))

        // Legitimate usage of findModelAggregate
        val source = env.queries.findModelAggregate(sourceRef)
        // Legitimate usage of findModelAggregate
        val copied = env.queries.findModelAggregate(copiedRef)
        assertNotEquals(source.id, copied.id)

        for (sourceType in source.types) {
            val copiedType = copied.findType(sourceType.key)
            assertNotEquals(sourceType.id, copiedType.id)
        }
        for (sourceEntity in source.entities) {
            val copiedEntity = copied.findEntity(sourceEntity.key)
            assertNotEquals(sourceEntity.id, copiedEntity.id)
        }
        for (sourceRelationship in source.relationships) {
            val copiedRelationship = copied.findRelationship(RelationshipRef.ByKey(sourceRelationship.key))
            assertNotEquals(sourceRelationship.id, copiedRelationship.id)
            for (sourceRole in sourceRelationship.roles) {
                val copiedRole = copiedRelationship.roles.first { role -> role.key == sourceRole.key }
                assertNotEquals(sourceRole.id, copiedRole.id)
            }
        }
        for (sourceAttribute in source.attributes) {
            val copiedAttribute = when (val ownerId = sourceAttribute.ownerId) {
                is AttributeOwnerId.OwnerEntityId -> {
                    val sourceOwnerKey = source.findEntity(ownerId.id).key
                    copied.findEntityAttribute(
                        EntityRef.ByKey(sourceOwnerKey),
                        EntityAttributeRef.ByKey(sourceAttribute.key)
                    )
                }

                is AttributeOwnerId.OwnerRelationshipId -> {
                    val sourceOwnerKey = source.findRelationship(ownerId.id).key
                    copied.findRelationshipAttributeOptional(
                        RelationshipRef.ByKey(sourceOwnerKey),
                        RelationshipAttributeRef.ByKey(sourceAttribute.key)
                    )!!
                }
            }
            assertNotEquals(sourceAttribute.id, copiedAttribute.id)
        }
    }

    @Test
    fun `copy model is rejected when destination key already exists`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-dup")
        val existingTargetKey = ModelKey("copy-target-dup")
        env.dispatch(
            ModelAction.Model_Create(
                key = sourceKey,
                name = LocalizedText("Source"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                key = existingTargetKey,
                name = LocalizedText("Already exists"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )

        assertFailsWith<ModelDuplicateKeyException> {
            env.dispatch(
                ModelAction.Model_Copy(
                    modelRef = modelRefKey(sourceKey),
                    modelNewKey = existingTargetKey
                )
            )
        }
    }

    @Test
    fun `copy model is rejected when source model does not exist`() {
        val env = ModelTestEnv()

        assertFailsWith<ModelNotFoundException> {
            env.dispatch(
                ModelAction.Model_Copy(
                    modelRef = modelRefKey("missing-source"),
                    modelNewKey = ModelKey("copy-target-missing-source")
                )
            )
        }
    }

    @Test
    fun `copy model works for a minimal model without entities relationships or attributes`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-min")
        val copiedKey = ModelKey("copy-target-min")
        env.dispatch(
            ModelAction.Model_Create(
                key = sourceKey,
                name = LocalizedText("Minimal model"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )

        env.dispatch(ModelAction.Model_Copy(modelRefKey(sourceKey), copiedKey))

        // Legitimate usage of findModelAggregate
        val copied = env.queries.findModelAggregateByKey(copiedKey)
        assertTrue(copied.types.isEmpty())
        assertTrue(copied.entities.isEmpty())
        assertTrue(copied.relationships.isEmpty())
        assertTrue(copied.attributes.isEmpty())
    }

    // ------------------------------------------------------------------------
    // Business content checks - Model
    // ------------------------------------------------------------------------

    @Test
    fun `copy model keeps same model name description version origin and documentationHome`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-model-fields")
        val copiedKey = ModelKey("copy-target-model-fields")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val sourceName = LocalizedText("Billing model")
        val sourceDescription = LocalizedMarkdown("Billing domain model")
        val sourceVersion = ModelVersion("3.4.5")
        val sourceDocHome = URI("https://example.org/models/billing").toString()

        env.dispatch(
            ModelAction.Model_Create(
                key = sourceKey,
                name = sourceName,
                description = sourceDescription,
                version = sourceVersion
            )
        )
        env.dispatch(ModelAction.Model_UpdateDocumentationHome(sourceRef, sourceDocHome))

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val source = env.queries.findModelRoot(sourceRef)
        val copied = env.queries.findModelRoot(copiedRef)
        assertEquals(source.name, copied.name)
        assertEquals(source.description, copied.description)
        assertEquals(source.version, copied.version)
        assertEquals(source.origin, copied.origin)
        assertEquals(source.documentationHome, copied.documentationHome)
    }

    @Test
    fun `copy model always sets copied model authority to system`() {
        val env = ModelTestEnv()
        val sourceRef = modelRefKey("copy-source-model-authority")
        val copiedRef = modelRefKey("copy-target-model-authority")

        env.dispatch(
            ModelAction.Model_Create(
                key = sourceRef.key,
                name = LocalizedText("Source model"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Model_UpdateAuthority(sourceRef, ModelAuthority.CANONICAL))

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedRef.key))

        val source = env.queries.findModelRoot(sourceRef)
        val copied = env.queries.findModelRoot(copiedRef)
        assertEquals(ModelAuthority.CANONICAL, source.authority)
        assertEquals(ModelAuthority.SYSTEM, copied.authority)
    }

    // ------------------------------------------------------------------------
    // Business content checks - Types
    // ------------------------------------------------------------------------

    @Test
    fun `copy model keeps same type keys names and descriptions`() {
        val env = ModelTestEnv()

        val sourceRef = modelRefKey("copy-source-types")
        val copiedRef = modelRefKey("copy-target-types")

        env.dispatch(
            ModelAction.Model_Create(
                key = sourceRef.key,
                name = LocalizedText("Type source"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = sourceRef,
                typeKey = TypeKey("String"),
                name = LocalizedText("String"),
                description = LocalizedMarkdown("Text type")
            )
        )
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = sourceRef,
                typeKey = TypeKey("Boolean"),
                name = LocalizedText("Boolean"),
                description = LocalizedMarkdown("Boolean type")
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedRef.key))

        val sourceTypes = env.queries.findTypes(sourceRef)
        val copiedTypes = env.queries.findTypes(copiedRef)
        assertEquals(sourceTypes.size, copiedTypes.size)

        for (sourceType in sourceTypes) {
            val copiedType = env.queries.findType(sourceRef, TypeRef.typeRefKey(sourceType.key))
            assertEquals(sourceType.name, copiedType.name)
            assertEquals(sourceType.description, copiedType.description)
        }
    }

    // ------------------------------------------------------------------------
    // Business content checks - Entities
    // ------------------------------------------------------------------------

    @Test
    fun `copy model keeps same entity keys names descriptions and documentationHome`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-entity-fields")
        val copiedKey = ModelKey("copy-target-entity-fields")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val entityKey = EntityKey("customer")
        val entityRef = EntityRef.ByKey(entityKey)
        val docHome = URI("https://example.org/entities/customer").toString()

        env.dispatch(
            ModelAction.Model_Create(
                sourceRef.key,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = sourceRef,
                entityKey = entityKey,
                name = LocalizedText("Customer"),
                description = LocalizedMarkdown("Customer aggregate"),
                documentationHome = docHome
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val source = env.queries.findEntity(sourceRef, entityRef)
        val copied = env.queries.findEntity(copiedRef, entityRef)
        assertEquals(source.key, copied.key)
        assertEquals(source.name, copied.name)
        assertEquals(source.description, copied.description)
        assertEquals(source.documentationHome, copied.documentationHome)
    }

    /**
     * After model copy, the copied entity must reference its own "id" attribute.
     *
     * This prevents a broken copy where the copied entity still references
     * the identity attribute from the source model.
     *
     * The test also checks that the copied primary key points to that same
     * copied identity attribute.
     */
    @Test
    fun `copy model keeps each entity identity attribute pointing to an attribute of that copied entity`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-identity-attr")
        val copiedKey = ModelKey("copy-target-identity-attr")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val entityRef = EntityRef.ByKey(EntityKey("customer"))

        env.dispatch(
            ModelAction.Model_Create(
                sourceRef.key,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, null))
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = sourceRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("id"),
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityPrimaryKey_Update(
                modelRef = sourceRef,
                entityRef = entityRef,
                attributeRef = listOf(EntityAttributeRef.ByKey(AttributeKey("id")))
            )
        )


        val sourcePrimaryKey = assertNotNull(env.queries.findEntityPrimaryKeyOptional(sourceRef, entityRef))
        val sourceIdentityAttributeId = sourcePrimaryKey.participants.first().attributeId

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))


        val copiedPrimaryKey = assertNotNull(env.queries.findEntityPrimaryKeyOptional(copiedRef, entityRef))

        assertEquals(1, copiedPrimaryKey.participants.size)
        val copiedIdentityAttributeId = copiedPrimaryKey.participants.first().attributeId
        val copiedIdentity = env.queries.findEntityAttributeOptional(
            copiedRef,
            entityRef,
            EntityAttributeRef.ById(copiedIdentityAttributeId)
        )
        assertNotNull(copiedIdentity)
        assertEquals(AttributeKey("id"), copiedIdentity.key)
        assertNotEquals(sourceIdentityAttributeId, copiedIdentityAttributeId)
    }

    // ------------------------------------------------------------------------
    // Business content checks - Entity attributes
    // ------------------------------------------------------------------------

    @Test
    fun `copy model keeps same entity attribute keys names descriptions optional flags and owner entity`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-entity-attrs")
        val copiedKey = ModelKey("copy-target-entity-attrs")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val entityRef = EntityRef.ByKey(EntityKey("customer"))
        val attributeRef = EntityAttributeRef.ByKey(AttributeKey("email"))

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, null))
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = sourceRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("email"),
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = true,
                name = LocalizedText("Email"),
                description = LocalizedMarkdown("Customer email")
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val sourceAttr = env.queries.findEntityAttribute(sourceRef, entityRef, attributeRef)
        val copiedAttr = env.queries.findEntityAttribute(copiedRef, entityRef, attributeRef)
        assertEquals(sourceAttr.key, copiedAttr.key)
        assertEquals(sourceAttr.name, copiedAttr.name)
        assertEquals(sourceAttr.description, copiedAttr.description)
        assertEquals(sourceAttr.optional, copiedAttr.optional)
        assertEquals(
            sourceAttr.ownerId is AttributeOwnerId.OwnerEntityId,
            copiedAttr.ownerId is AttributeOwnerId.OwnerEntityId
        )
    }

    @Test
    fun `copy model entity attributes point to copied types with the same type keys`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-entity-attr-type")
        val copiedKey = ModelKey("copy-target-entity-attr-type")
        val sourceRef = modelRefKey(sourceKey)
        val entityRef = EntityRef.ByKey(EntityKey("customer"))
        val attributeRef = EntityAttributeRef.ByKey(AttributeKey("vip"))

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("Boolean"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, null))
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = sourceRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("vip"),
                type = TypeRef.typeRefKey(TypeKey("Boolean")),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copiedRef = modelRefKey(copiedKey)
        val copiedAttr = env.queries.findEntityAttribute(copiedRef, entityRef, attributeRef)
        val copiedType = env.queries.findType(copiedRef, typeRefId(copiedAttr.typeId))
        assertEquals(TypeKey("Boolean"), copiedType.key)
    }

    // ------------------------------------------------------------------------
    // Business content checks - Relationships
    // ------------------------------------------------------------------------

    @Test
    fun `copy model keeps same relationship keys names and descriptions`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-rel-fields")
        val copiedKey = ModelKey("copy-target-rel-fields")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val customerRef = EntityRef.ByKey(EntityKey("customer"))
        val orderRef = EntityRef.ByKey(EntityKey("order"))
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, null))
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = sourceRef,
                relationshipKey = RelationshipKey("places"),
                name = LocalizedText("Places"),
                description = LocalizedMarkdown("Customer places an order"),
                roleAKey = RelationshipRoleKey("buyer"),
                roleAEntityRef = customerRef,
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("purchase"),
                roleBEntityRef = orderRef,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val sourceRel = env.queries.findRelationship(sourceRef, relationshipRef)
        val copiedRel = env.queries.findRelationship(copiedRef, relationshipRef)
        assertEquals(sourceRel.key, copiedRel.key)
        assertEquals(sourceRel.name, copiedRel.name)
        assertEquals(sourceRel.description, copiedRel.description)
    }

    @Test
    fun `copy model keeps relationship roles with same keys names and cardinalities`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-role-fields")
        val copiedKey = ModelKey("copy-target-role-fields")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, null))
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = sourceRef,
                relationshipKey = RelationshipKey("places"),
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("buyer"),
                roleAEntityRef = EntityRef.ByKey(EntityKey("customer")),
                roleAName = LocalizedText("Buyer"),
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("purchase"),
                roleBEntityRef = EntityRef.ByKey(EntityKey("order")),
                roleBName = LocalizedText("Purchase"),
                roleBCardinality = RelationshipCardinality.Many
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val sourceRel = env.queries.findRelationship(sourceRef,relationshipRef)
        val copiedRel = env.queries.findRelationship(copiedRef,relationshipRef)
        assertEquals(sourceRel.roles.size, copiedRel.roles.size)
        for (sourceRole in sourceRel.roles) {
            val copiedRole = copiedRel.roles.first { role -> role.key == sourceRole.key }
            assertEquals(sourceRole.name, copiedRole.name)
            assertEquals(sourceRole.cardinality, copiedRole.cardinality)
        }
    }

    @Test
    fun `copy model keeps each relationship role pointing to the copied entity matching the same entity key`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-role-entity")
        val copiedKey = ModelKey("copy-target-role-entity")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, null))
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = sourceRef,
                relationshipKey = RelationshipKey("places"),
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("buyer"),
                roleAEntityRef = EntityRef.ByKey(EntityKey("customer")),
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("purchase"),
                roleBEntityRef = EntityRef.ByKey(EntityKey("order")),
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val sourceRelationship = env.queries.findRelationship(sourceRef, relationshipRef)
        val copiedRelationship = env.queries.findRelationship(copiedRef, relationshipRef)
        for (sourceRole in sourceRelationship.roles) {
            val copiedRole = copiedRelationship.roles.first { role -> role.key == sourceRole.key }
            val sourceEntityKey = env.queries.findEntity(sourceRef, entityRefId(sourceRole.entityId)).key
            val copiedEntityKey = env.queries.findEntity(copiedRef, entityRefId(copiedRole.entityId)).key
            assertEquals(sourceEntityKey, copiedEntityKey)
            assertNotEquals(sourceRole.id, copiedRole.id)
        }
    }

    /**
     * Self-reference case: multiple roles in one relationship point to the same entity key
     * (for example, Person mentors Person).
     *
     * Example:
     * - Source model has entity key "Person" with id E1.
     * - Relationship "mentors" has two roles: "mentor" -> E1 and "mentee" -> E1.
     * - Copied model has entity key "Person" with id E2.
     * - Expected: both copied roles point to E2 (never to E1).
     *
     * After copy, each role must target the copied entity for that key, never an entity
     * from the source model.
     */
    @Test
    fun `copy model keeps roles targeting same entity key on copied self reference relationships`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-self-rel")
        val copiedKey = ModelKey("copy-target-self-rel")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("mentors"))
        val personRef = EntityRef.ByKey(EntityKey("person"))

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("person"), null, null, null))
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = sourceRef,
                relationshipKey = RelationshipKey("mentors"),
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("mentor"),
                roleAEntityRef = personRef,
                roleAName = null,
                roleACardinality = RelationshipCardinality.Many,
                roleBKey = RelationshipRoleKey("mentee"),
                roleBEntityRef = personRef,
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val sourcePersonId = env.queries.findEntity(sourceRef, personRef).id
        val copiedPersonId = env.queries.findEntity(copiedRef, personRef).id
        val copiedRelationship = env.queries.findRelationship(copiedRef, relationshipRef)
        val mentorRole = copiedRelationship.roles.first { role -> role.key == RelationshipRoleKey("mentor") }
        val menteeRole = copiedRelationship.roles.first { role -> role.key == RelationshipRoleKey("mentee") }
        assertEquals(copiedPersonId, mentorRole.entityId)
        assertEquals(copiedPersonId, menteeRole.entityId)
        assertNotEquals(sourcePersonId, mentorRole.entityId)
        assertNotEquals(sourcePersonId, menteeRole.entityId)
    }

    // ------------------------------------------------------------------------
    // Business content checks - Relationship attributes
    // ------------------------------------------------------------------------

    @Test
    fun `copy model keeps same relationship attribute keys names descriptions optional flags and owner relationship`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-rel-attrs")
        val copiedKey = ModelKey("copy-target-rel-attrs")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))
        val attributeRef = RelationshipAttributeRef.ByKey(AttributeKey("startedAt"))

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, null))
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = sourceRef,
                relationshipKey = RelationshipKey("places"),
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("buyer"),
                roleAEntityRef = EntityRef.ByKey(EntityKey("customer")),
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("purchase"),
                roleBEntityRef = EntityRef.ByKey(EntityKey("order")),
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = sourceRef,
                relationshipRef = relationshipRef,
                attributeKey = AttributeKey("startedAt"),
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = true,
                name = LocalizedText("Started at"),
                description = LocalizedMarkdown("Relationship start time")
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val sourceAttr = env.queries.findRelationshipAttribute(sourceRef, relationshipRef, attributeRef)
        val copiedAttr = env.queries.findRelationshipAttribute(copiedRef, relationshipRef, attributeRef)
        assertEquals(sourceAttr.key, copiedAttr.key)
        assertEquals(sourceAttr.name, copiedAttr.name)
        assertEquals(sourceAttr.description, copiedAttr.description)
        assertEquals(sourceAttr.optional, copiedAttr.optional)
        assertEquals(
            sourceAttr.ownerId is AttributeOwnerId.OwnerRelationshipId,
            copiedAttr.ownerId is AttributeOwnerId.OwnerRelationshipId
        )
    }

    @Test
    fun `copy model relationship attributes point to copied types with the same type keys`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-rel-attr-type")
        val copiedKey = ModelKey("copy-target-rel-attr-type")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))
        val attributeRef = RelationshipAttributeRef.ByKey(AttributeKey("confirmed"))

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("Boolean"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, null))
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = sourceRef,
                relationshipKey = RelationshipKey("places"),
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("buyer"),
                roleAEntityRef = EntityRef.ByKey(EntityKey("customer")),
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("purchase"),
                roleBEntityRef = EntityRef.ByKey(EntityKey("order")),
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = sourceRef,
                relationshipRef = relationshipRef,
                attributeKey = AttributeKey("confirmed"),
                type = TypeRef.typeRefKey(TypeKey("Boolean")),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copiedAttr = env.queries.findRelationshipAttributeOptional(copiedRef, relationshipRef, attributeRef)!!
        val copiedType = env.queries.findType(copiedRef, typeRefId(copiedAttr.typeId))
        assertEquals(TypeKey("Boolean"), copiedType.key)
    }

    // ------------------------------------------------------------------------
    // Tags business rules
    // ------------------------------------------------------------------------

    @Test
    fun `copy model recreates local model tags with same keys names descriptions new ids and copied model local scope`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-local-model-tag")
        val copiedKey = ModelKey("copy-target-local-model-tag")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val sourceTagKey = TagKey("local-model-tag")
        val sourceTagName = "Local model tag"
        val sourceTagDescription = "Model local tag description"

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        val sourceModelId = env.queries.findModelRoot(sourceRef).id
        val sourceScope = ModelTagResolver.modelTagScopeRef(sourceModelId)
        val sourceTagRef = TagRef.ByKey(sourceScope, null, sourceTagKey)
        env.dispatchTag(TagAction.TagLocalCreate(sourceScope, sourceTagKey, sourceTagName, sourceTagDescription))
        val sourceTag = env.tagQueries.findTagByRef(sourceTagRef)
        env.dispatch(ModelAction.Model_AddTag(sourceRef, sourceTagRef))

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copied = env.queries.findModelRoot(copiedRef)
        val modelTagIds = env.queries.findModelTags(copiedRef)
        assertEquals(1, modelTagIds.size)
        assertNotEquals(sourceTag.id, modelTagIds[0])
        val copiedTag = env.tagQueries.findTagByIdOptional(modelTagIds[0])!!
        val copiedScope = ModelTagResolver.modelTagScopeRef(copied.id)
        assertEquals(sourceTag.key, copiedTag.key)
        assertEquals(sourceTag.name, copiedTag.name)
        assertEquals(sourceTag.description, copiedTag.description)
        assertEquals(copiedScope, copiedTag.scope)
    }

    @Test
    fun `copy model recreates unassigned local model tags in copied model local scope`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("model-source")
        val copiedKey = ModelKey("model-dest")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val localTagKey = TagKey("rgpd")
        val localTagName = "RGPD"
        val localTagDescription = "Personal data"

        // Arrange: create source model and a local tag in its scope, without attaching it.
        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        val sourceModelId = env.queries.findModelRoot(sourceRef).id
        val sourceScope = ModelTagResolver.modelTagScopeRef(sourceModelId)
        val sourceTagRef = TagRef.ByKey(sourceScope, null, localTagKey)
        env.dispatchTag(TagAction.TagLocalCreate(sourceScope, localTagKey, localTagName, localTagDescription))
        val sourceLocalTag = env.tagQueries.findTagByRef(sourceTagRef)

        // Act: copy the model.
        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        // Assert: the unassigned tag must remain unassigned in the copied model
        val copiedTags = env.queries.findModelTags(copiedRef)
        assertTrue(copiedTags.isEmpty())

        // Assert: the copied model local scope contains an equivalent recreated tag.
        val copied = env.queries.findModelRoot(copiedRef)
        val copiedScope = ModelTagResolver.modelTagScopeRef(copied.id)
        val copiedLocalTags = env.tagQueries.findAllTagByScopeRef(copiedScope)
        val copiedLocalTag = copiedLocalTags.firstOrNull { tag -> tag.key == localTagKey }
        assertNotNull(copiedLocalTag)
        assertNotEquals(sourceLocalTag.id, copiedLocalTag.id)
        assertEquals(localTagName, copiedLocalTag.name)
        assertEquals(localTagDescription, copiedLocalTag.description)
    }

    @Test
    fun `copy model recreates local entity tags with same keys names descriptions new ids and copied model local scope`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-local-entity-tag")
        val copiedKey = ModelKey("copy-target-local-entity-tag")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val entityRef = EntityRef.ByKey(EntityKey("customer"))
        val sourceTagKey = TagKey("local-entity-tag")

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, null))
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = sourceRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("id"),
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        val sourceModelId = env.queries.findModelRoot(sourceRef).id
        val sourceScope = ModelTagResolver.modelTagScopeRef(sourceModelId)
        val sourceTagRef = TagRef.ByKey(sourceScope, null, sourceTagKey)
        env.dispatchTag(TagAction.TagLocalCreate(sourceScope, sourceTagKey, "Entity local", "Entity local tag"))
        val sourceTag = env.tagQueries.findTagByRef(sourceTagRef)
        env.dispatch(ModelAction.Entity_AddTag(sourceRef, entityRef, sourceTagRef))

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copiedModel = env.queries.findModelRoot(copiedRef)
        val copiedEntity = env.queries.findEntity(copiedRef, entityRef)
        assertEquals(1, copiedEntity.tags.size)
        assertNotEquals(sourceTag.id, copiedEntity.tags[0])
        val copiedTag = env.tagQueries.findTagByIdOptional(copiedEntity.tags[0])!!
        assertEquals(sourceTag.key, copiedTag.key)
        assertEquals(sourceTag.name, copiedTag.name)
        assertEquals(sourceTag.description, copiedTag.description)
        assertEquals(ModelTagResolver.modelTagScopeRef(copiedModel.id), copiedTag.scope)
    }

    @Test
    fun `copy model recreates local entity attribute tags with same keys names descriptions new ids and copied model local scope`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-local-entity-attr-tag")
        val copiedKey = ModelKey("copy-target-local-entity-attr-tag")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val entityRef = EntityRef.ByKey(EntityKey("customer"))
        val attributeRef = EntityAttributeRef.ByKey(AttributeKey("id"))
        val sourceTagKey = TagKey("local-entity-attr-tag")

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, null))
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = sourceRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("id"),
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        val sourceModelId = env.queries.findModelRoot(sourceRef).id
        val sourceScope = ModelTagResolver.modelTagScopeRef(sourceModelId)
        val sourceTagRef = TagRef.ByKey(sourceScope, null, sourceTagKey)
        env.dispatchTag(
            TagAction.TagLocalCreate(
                sourceScope,
                sourceTagKey,
                "Entity attr local",
                "Entity attribute local tag"
            )
        )
        val sourceTag = env.tagQueries.findTagByRef(sourceTagRef)
        env.dispatch(ModelAction.EntityAttribute_AddTag(sourceRef, entityRef, attributeRef, sourceTagRef))

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copiedAttr = env.queries.findEntityAttribute(copiedRef, entityRef, attributeRef)
        assertEquals(1, copiedAttr.tags.size)
        assertNotEquals(sourceTag.id, copiedAttr.tags[0])
        val copiedTag = env.tagQueries.findTagByIdOptional(copiedAttr.tags[0])!!
        val copiedModel = env.queries.findModelRoot(copiedRef)
        val copiedScope = ModelTagResolver.modelTagScopeRef(copiedModel.id)
        assertEquals(sourceTag.key, copiedTag.key)
        assertEquals(sourceTag.name, copiedTag.name)
        assertEquals(sourceTag.description, copiedTag.description)
        assertEquals(copiedScope, copiedTag.scope)
    }

    @Test
    fun `copy model recreates local relationship tags with same keys names descriptions new ids and copied model local scope`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-local-rel-tag")
        val copiedKey = ModelKey("copy-target-local-rel-tag")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))
        val sourceTagKey = TagKey("local-rel-tag")

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, null))
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = sourceRef,
                relationshipKey = RelationshipKey("places"),
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("buyer"),
                roleAEntityRef = EntityRef.ByKey(EntityKey("customer")),
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("purchase"),
                roleBEntityRef = EntityRef.ByKey(EntityKey("order")),
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        val sourceModelId = env.queries.findModelRoot(sourceRef).id
        val sourceScope = ModelTagResolver.modelTagScopeRef(sourceModelId)
        val sourceTagRef = TagRef.ByKey(sourceScope, null, sourceTagKey)
        env.dispatchTag(
            TagAction.TagLocalCreate(
                sourceScope,
                sourceTagKey,
                "Relationship local",
                "Relationship local tag"
            )
        )
        val sourceTag = env.tagQueries.findTagByRef(sourceTagRef)
        env.dispatch(ModelAction.Relationship_AddTag(sourceRef, relationshipRef, sourceTagRef))

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copiedModel = env.queries.findModelRoot(copiedRef)
        val copiedRel = env.queries.findRelationship(copiedRef, relationshipRef)
        assertEquals(1, copiedRel.tags.size)
        assertNotEquals(sourceTag.id, copiedRel.tags[0])
        val copiedTag = env.tagQueries.findTagByIdOptional(copiedRel.tags[0])!!
        val copiedScope = ModelTagResolver.modelTagScopeRef(copiedModel.id)
        assertEquals(sourceTag.key, copiedTag.key)
        assertEquals(sourceTag.name, copiedTag.name)
        assertEquals(sourceTag.description, copiedTag.description)
        assertEquals(copiedScope, copiedTag.scope)
    }

    @Test
    fun `copy model recreates local relationship attribute tags with same keys names descriptions new ids and copied model local scope`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-local-rel-attr-tag")
        val copiedKey = ModelKey("copy-target-local-rel-attr-tag")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))
        val relationshipAttributeRef = RelationshipAttributeRef.ByKey(AttributeKey("confirmed"))
        val sourceTagKey = TagKey("local-rel-attr-tag")

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("Boolean"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, null))
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = sourceRef,
                relationshipKey = RelationshipKey("places"),
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("buyer"),
                roleAEntityRef = EntityRef.ByKey(EntityKey("customer")),
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("purchase"),
                roleBEntityRef = EntityRef.ByKey(EntityKey("order")),
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = sourceRef,
                relationshipRef = relationshipRef,
                attributeKey = AttributeKey("confirmed"),
                type = TypeRef.typeRefKey(TypeKey("Boolean")),
                optional = false,
                name = null,
                description = null
            )
        )
        val sourceModelId = env.queries.findModelRoot(sourceRef).id
        val sourceScope = ModelTagResolver.modelTagScopeRef(sourceModelId)
        val sourceTagRef = TagRef.ByKey(sourceScope, null, sourceTagKey)
        env.dispatchTag(
            TagAction.TagLocalCreate(
                sourceScope,
                sourceTagKey,
                "Relationship attr local",
                "Relationship attribute local tag"
            )
        )
        val sourceTag = env.tagQueries.findTagByRef(sourceTagRef)
        env.dispatch(
            ModelAction.RelationshipAttribute_AddTag(
                sourceRef,
                relationshipRef,
                relationshipAttributeRef,
                sourceTagRef
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copiedModel = env.queries.findModelRoot(copiedRef)
        val copiedAttr = env.queries.findRelationshipAttribute(copiedRef, relationshipRef, relationshipAttributeRef)
        assertEquals(1, copiedAttr.tags.size)
        assertNotEquals(sourceTag.id, copiedAttr.tags[0])
        val copiedTag = env.tagQueries.findTagByIdOptional(copiedAttr.tags[0])!!
        val copiedScope = ModelTagResolver.modelTagScopeRef(copiedModel.id)
        assertEquals(sourceTag.key, copiedTag.key)
        assertEquals(sourceTag.name, copiedTag.name)
        assertEquals(sourceTag.description, copiedTag.description)
        assertEquals(copiedScope, copiedTag.scope)
    }

    @Test
    fun `copy model keeps same global tag ids on model entities entity attributes relationships and relationship attributes`() {
        val env = ModelTestEnv()
        val sourceKey = ModelKey("copy-source-global-tags")
        val copiedKey = ModelKey("copy-target-global-tags")
        val sourceRef = modelRefKey(sourceKey)
        val copiedRef = modelRefKey(copiedKey)
        val entityRef = EntityRef.ByKey(EntityKey("customer"))
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))
        val entityAttributeRef = EntityAttributeRef.ByKey(AttributeKey("id"))
        val relationshipAttributeRef = RelationshipAttributeRef.ByKey(AttributeKey("confirmed"))
        val globalTag = env.createGlobalTag("copy-global-group", "copy-global-tag")

        env.dispatch(
            ModelAction.Model_Create(
                sourceKey,
                LocalizedText("Source"),
                null,
                ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("Boolean"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, null))
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = sourceRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("id"),
                type = TypeRef.typeRefKey(TypeKey("String")),
                optional = false,
                name = null,
                description = null
            )
        )
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = sourceRef,
                relationshipKey = RelationshipKey("places"),
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("buyer"),
                roleAEntityRef = EntityRef.ByKey(EntityKey("customer")),
                roleAName = null,
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("purchase"),
                roleBEntityRef = EntityRef.ByKey(EntityKey("order")),
                roleBName = null,
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = sourceRef,
                relationshipRef = relationshipRef,
                attributeKey = AttributeKey("confirmed"),
                type = TypeRef.typeRefKey(TypeKey("Boolean")),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(ModelAction.Model_AddTag(sourceRef, globalTag.ref))
        env.dispatch(ModelAction.Entity_AddTag(sourceRef, entityRef, globalTag.ref))
        env.dispatch(ModelAction.EntityAttribute_AddTag(sourceRef, entityRef, entityAttributeRef, globalTag.ref))
        env.dispatch(ModelAction.Relationship_AddTag(sourceRef, relationshipRef, globalTag.ref))
        env.dispatch(
            ModelAction.RelationshipAttribute_AddTag(
                sourceRef,
                relationshipRef,
                relationshipAttributeRef,
                globalTag.ref
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))


        val copiedModel = env.queries.findModelRoot(copiedRef)
        assertEquals(listOf(globalTag.id), env.queries.findModelTags(copiedRef))
        assertEquals(listOf(globalTag.id), env.queries.findEntity(copiedRef, entityRef).tags)
        assertEquals(
            listOf(globalTag.id),
            env.queries.findEntityAttribute(copiedRef, entityRef, entityAttributeRef).tags
        )
        assertEquals(listOf(globalTag.id), env.queries.findRelationship(copiedRef, relationshipRef).tags)
        assertEquals(
            listOf(globalTag.id),
            env.queries.findRelationshipAttributeOptional(copiedRef, relationshipRef, relationshipAttributeRef)!!.tags
        )
    }

}
