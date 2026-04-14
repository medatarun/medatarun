package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.ports.needs.ModelTagResolver
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
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-core")
        val copiedKey = ModelKey("copy-target-core")
        val sourceName = LocalizedTextNotLocalized("Source model")

        val version = ModelVersion("1.2.3")
        env.dispatch(
            ModelAction.Model_Create(
                key = sourceKey,
                name = sourceName,
                description = LocalizedMarkdownNotLocalized("Source description"),
                version = version
            )
        )

        env.dispatch(
            ModelAction.Model_Copy(
                modelRef = modelRefKey(sourceKey),
                modelNewKey = copiedKey
            )
        )

        val source = env.queries.findModelAggregateByKey(sourceKey)
        val copied = env.queries.findModelAggregateByKey(copiedKey)
        assertEquals(copiedKey, copied.key)
        assertEquals(sourceName, copied.name)
        assertNotEquals(source.id, copied.id)

        // Creating a model immediately creates a first version
        env.assertUniqueVersion(version, copied.id)
    }

    @Test
    fun `copy model does not modify the source model`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-unchanged")
        val copiedKey = ModelKey("copy-target-unchanged")

        env.dispatch(
            ModelAction.Model_Create(
                key = sourceKey,
                name = LocalizedTextNotLocalized("Source model"),
                description = LocalizedMarkdownNotLocalized("Before copy"),
                version = ModelVersion("2.0.0")
            )
        )
        val sourceBefore = env.queries.findModelAggregateByKey(sourceKey)

        env.dispatch(
            ModelAction.Model_Copy(
                modelRef = modelRefKey(sourceKey),
                modelNewKey = copiedKey
            )
        )

        val sourceAfter = env.queries.findModelAggregateByKey(sourceKey)
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
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-ids")
        val copiedKey = ModelKey("copy-target-ids")
        val sourceRef = modelRefKey(sourceKey)
        val customerRef = EntityRef.ByKey(EntityKey("customer"))
        val orderRef = EntityRef.ByKey(EntityKey("order"))
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("Boolean"), null, null))
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = sourceRef,
                entityKey = EntityKey("customer"),
                name = LocalizedTextNotLocalized("Customer"),
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = sourceRef,
                entityKey = EntityKey("order"),
                name = LocalizedTextNotLocalized("Order"),
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = sourceRef,
                entityRef = customerRef,
                attributeKey = AttributeKey("vip"),
                type = typeRef("Boolean"),
                optional = false,
                name = LocalizedTextNotLocalized("VIP"),
                description = null
            )
        )
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = sourceRef,
                relationshipKey = RelationshipKey("places"),
                name = LocalizedTextNotLocalized("Places"),
                description = null,
                roleAKey = RelationshipRoleKey("buyer"),
                roleAEntityRef = customerRef,
                roleAName = LocalizedTextNotLocalized("Buyer"),
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("purchase"),
                roleBEntityRef = orderRef,
                roleBName = LocalizedTextNotLocalized("Purchase"),
                roleBCardinality = RelationshipCardinality.Many
            )
        )
        env.dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = sourceRef,
                relationshipRef = relationshipRef,
                attributeKey = AttributeKey("confirmed"),
                type = typeRef("Boolean"),
                optional = false,
                name = LocalizedTextNotLocalized("Confirmed"),
                description = null
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val source = env.queries.findModelAggregateByKey(sourceKey)
        val copied = env.queries.findModelAggregateByKey(copiedKey)
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
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-dup")
        val existingTargetKey = ModelKey("copy-target-dup")
        env.dispatch(
            ModelAction.Model_Create(
                key = sourceKey,
                name = LocalizedTextNotLocalized("Source"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Model_Create(
                key = existingTargetKey,
                name = LocalizedTextNotLocalized("Already exists"),
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
        val env = createEnv()

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
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-min")
        val copiedKey = ModelKey("copy-target-min")
        env.dispatch(
            ModelAction.Model_Create(
                key = sourceKey,
                name = LocalizedTextNotLocalized("Minimal model"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )

        env.dispatch(ModelAction.Model_Copy(modelRefKey(sourceKey), copiedKey))

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
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-model-fields")
        val copiedKey = ModelKey("copy-target-model-fields")
        val sourceRef = modelRefKey(sourceKey)
        val sourceName = LocalizedTextNotLocalized("Billing model")
        val sourceDescription = LocalizedMarkdownNotLocalized("Billing domain model")
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

        val source = env.queries.findModelAggregateByKey(sourceKey)
        val copied = env.queries.findModelAggregateByKey(copiedKey)
        assertEquals(source.name, copied.name)
        assertEquals(source.description, copied.description)
        assertEquals(source.version, copied.version)
        assertEquals(source.origin, copied.origin)
        assertEquals(source.documentationHome, copied.documentationHome)
    }

    @Test
    fun `copy model always sets copied model authority to system`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-model-authority")
        val copiedKey = ModelKey("copy-target-model-authority")
        val sourceRef = modelRefKey(sourceKey)

        env.dispatch(
            ModelAction.Model_Create(
                key = sourceKey,
                name = LocalizedTextNotLocalized("Source model"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Model_UpdateAuthority(sourceRef, ModelAuthority.CANONICAL))

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val source = env.queries.findModelAggregateByKey(sourceKey)
        val copied = env.queries.findModelAggregateByKey(copiedKey)
        assertEquals(ModelAuthority.CANONICAL, source.authority)
        assertEquals(ModelAuthority.SYSTEM, copied.authority)
    }

    // ------------------------------------------------------------------------
    // Business content checks - Types
    // ------------------------------------------------------------------------

    @Test
    fun `copy model keeps same type keys names and descriptions`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-types")
        val copiedKey = ModelKey("copy-target-types")
        val sourceRef = modelRefKey(sourceKey)
        env.dispatch(
            ModelAction.Model_Create(
                key = sourceKey,
                name = LocalizedTextNotLocalized("Type source"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = sourceRef,
                typeKey = TypeKey("String"),
                name = LocalizedTextNotLocalized("String"),
                description = LocalizedMarkdownNotLocalized("Text type")
            )
        )
        env.dispatch(
            ModelAction.Type_Create(
                modelRef = sourceRef,
                typeKey = TypeKey("Boolean"),
                name = LocalizedTextNotLocalized("Boolean"),
                description = LocalizedMarkdownNotLocalized("Boolean type")
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val source = env.queries.findModelAggregateByKey(sourceKey)
        val copied = env.queries.findModelAggregateByKey(copiedKey)
        assertEquals(source.types.size, copied.types.size)
        for (sourceType in source.types) {
            val copiedType = copied.findType(sourceType.key)
            assertEquals(sourceType.name, copiedType.name)
            assertEquals(sourceType.description, copiedType.description)
        }
    }

    // ------------------------------------------------------------------------
    // Business content checks - Entities
    // ------------------------------------------------------------------------

    @Test
    fun `copy model keeps same entity keys names descriptions and documentationHome`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-entity-fields")
        val copiedKey = ModelKey("copy-target-entity-fields")
        val sourceRef = modelRefKey(sourceKey)
        val entityKey = EntityKey("customer")
        val entityRef = EntityRef.ByKey(entityKey)
        val docHome = URI("https://example.org/entities/customer").toString()

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = sourceRef,
                entityKey = entityKey,
                name = LocalizedTextNotLocalized("Customer"),
                description = LocalizedMarkdownNotLocalized("Customer aggregate"),
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = docHome
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val source = env.queries.findModelAggregateByKey(sourceKey).findEntity(entityRef)
        val copied = env.queries.findModelAggregateByKey(copiedKey).findEntity(entityRef)
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
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-identity-attr")
        val copiedKey = ModelKey("copy-target-identity-attr")
        val sourceRef = modelRefKey(sourceKey)
        val entityRef = EntityRef.ByKey(EntityKey("customer"))

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = sourceRef,
                entityKey = EntityKey("customer"),
                name = null,
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )

        val sourceModel = env.queries.findModelAggregateByKey(sourceKey)
        val sourceEntity = sourceModel.findEntity(entityRef)
        val sourcePrimaryKey = assertNotNull(sourceModel.findEntityPrimaryKeyOptional(sourceEntity.id))
        val sourceIdentityAttributeId = sourcePrimaryKey.participants.first().attributeId

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copiedModel = env.queries.findModelAggregateByKey(copiedKey)
        val copiedEntity = copiedModel.findEntity(entityRef)
        val copiedPrimaryKey = assertNotNull(copiedModel.findEntityPrimaryKeyOptional(copiedEntity.id))

        assertEquals(copiedEntity.id, copiedPrimaryKey.entityId)
        assertEquals(1, copiedPrimaryKey.participants.size)
        val copiedIdentityAttributeId = copiedPrimaryKey.participants.first().attributeId
        val copiedIdentity = copiedModel.findEntityAttributeOptional(
            copiedEntity.ref,
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
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-entity-attrs")
        val copiedKey = ModelKey("copy-target-entity-attrs")
        val sourceRef = modelRefKey(sourceKey)
        val entityRef = EntityRef.ByKey(EntityKey("customer"))
        val attributeRef = EntityAttributeRef.ByKey(AttributeKey("email"))

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = sourceRef,
                entityKey = EntityKey("customer"),
                name = null,
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = sourceRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("email"),
                type = typeRef("String"),
                optional = true,
                name = LocalizedTextNotLocalized("Email"),
                description = LocalizedMarkdownNotLocalized("Customer email")
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val sourceAttr = env.queries.findEntityAttribute(sourceRef, entityRef, attributeRef)
        val copiedRef = modelRefKey(copiedKey)
        val copiedAttr = env.queries.findEntityAttribute(copiedRef, entityRef, attributeRef)
        assertEquals(sourceAttr.key, copiedAttr.key)
        assertEquals(sourceAttr.name, copiedAttr.name)
        assertEquals(sourceAttr.description, copiedAttr.description)
        assertEquals(sourceAttr.optional, copiedAttr.optional)
        assertEquals(sourceAttr.ownerId is AttributeOwnerId.OwnerEntityId, copiedAttr.ownerId is AttributeOwnerId.OwnerEntityId)
    }

    @Test
    fun `copy model entity attributes point to copied types with the same type keys`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-entity-attr-type")
        val copiedKey = ModelKey("copy-target-entity-attr-type")
        val sourceRef = modelRefKey(sourceKey)
        val entityRef = EntityRef.ByKey(EntityKey("customer"))
        val attributeRef = EntityAttributeRef.ByKey(AttributeKey("vip"))

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("Boolean"), null, null))
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = sourceRef,
                entityKey = EntityKey("customer"),
                name = null,
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = sourceRef,
                entityRef = entityRef,
                attributeKey = AttributeKey("vip"),
                type = typeRef("Boolean"),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copiedRef = modelRefKey(copiedKey)
        val copiedModel = env.queries.findModelAggregate(copiedRef)
        val copiedAttr = env.queries.findEntityAttribute(copiedRef, entityRef, attributeRef)
        val copiedType = copiedModel.findType(copiedAttr.typeId)
        assertEquals(TypeKey("Boolean"), copiedType.key)
    }

    // ------------------------------------------------------------------------
    // Business content checks - Relationships
    // ------------------------------------------------------------------------

    @Test
    fun `copy model keeps same relationship keys names and descriptions`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-rel-fields")
        val copiedKey = ModelKey("copy-target-rel-fields")
        val sourceRef = modelRefKey(sourceKey)
        val customerRef = EntityRef.ByKey(EntityKey("customer"))
        val orderRef = EntityRef.ByKey(EntityKey("order"))
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, AttributeKey("id"), typeRef("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, AttributeKey("id"), typeRef("String"), null, null))
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = sourceRef,
                relationshipKey = RelationshipKey("places"),
                name = LocalizedTextNotLocalized("Places"),
                description = LocalizedMarkdownNotLocalized("Customer places an order"),
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

        val sourceRel = env.queries.findModelAggregate(sourceRef).findRelationship(relationshipRef)
        val copiedRel = env.queries.findModelAggregateByKey(copiedKey).findRelationship(relationshipRef)
        assertEquals(sourceRel.key, copiedRel.key)
        assertEquals(sourceRel.name, copiedRel.name)
        assertEquals(sourceRel.description, copiedRel.description)
    }

    @Test
    fun `copy model keeps relationship roles with same keys names and cardinalities`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-role-fields")
        val copiedKey = ModelKey("copy-target-role-fields")
        val sourceRef = modelRefKey(sourceKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, AttributeKey("id"), typeRef("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, AttributeKey("id"), typeRef("String"), null, null))
        env.dispatch(
            ModelAction.Relationship_Create(
                modelRef = sourceRef,
                relationshipKey = RelationshipKey("places"),
                name = null,
                description = null,
                roleAKey = RelationshipRoleKey("buyer"),
                roleAEntityRef = EntityRef.ByKey(EntityKey("customer")),
                roleAName = LocalizedTextNotLocalized("Buyer"),
                roleACardinality = RelationshipCardinality.One,
                roleBKey = RelationshipRoleKey("purchase"),
                roleBEntityRef = EntityRef.ByKey(EntityKey("order")),
                roleBName = LocalizedTextNotLocalized("Purchase"),
                roleBCardinality = RelationshipCardinality.Many
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val sourceRel = env.queries.findModelAggregate(sourceRef).findRelationship(relationshipRef)
        val copiedRel = env.queries.findModelAggregateByKey(copiedKey).findRelationship(relationshipRef)
        assertEquals(sourceRel.roles.size, copiedRel.roles.size)
        for (sourceRole in sourceRel.roles) {
            val copiedRole = copiedRel.roles.first { role -> role.key == sourceRole.key }
            assertEquals(sourceRole.name, copiedRole.name)
            assertEquals(sourceRole.cardinality, copiedRole.cardinality)
        }
    }

    @Test
    fun `copy model keeps each relationship role pointing to the copied entity matching the same entity key`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-role-entity")
        val copiedKey = ModelKey("copy-target-role-entity")
        val sourceRef = modelRefKey(sourceKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, AttributeKey("id"), typeRef("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, AttributeKey("id"), typeRef("String"), null, null))
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

        val sourceModel = env.queries.findModelAggregate(sourceRef)
        val copiedModel = env.queries.findModelAggregateByKey(copiedKey)
        val sourceRelationship = sourceModel.findRelationship(relationshipRef)
        val copiedRelationship = copiedModel.findRelationship(relationshipRef)
        for (sourceRole in sourceRelationship.roles) {
            val copiedRole = copiedRelationship.roles.first { role -> role.key == sourceRole.key }
            val sourceEntityKey = sourceModel.findEntity(sourceRole.entityId).key
            val copiedEntityKey = copiedModel.findEntity(copiedRole.entityId).key
            assertEquals(sourceEntityKey, copiedEntityKey)
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
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-self-rel")
        val copiedKey = ModelKey("copy-target-self-rel")
        val sourceRef = modelRefKey(sourceKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("mentors"))
        val personRef = EntityRef.ByKey(EntityKey("person"))

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("person"), null, null, AttributeKey("id"), typeRef("String"), null, null))
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

        val sourceModel = env.queries.findModelAggregate(sourceRef)
        val copiedModel = env.queries.findModelAggregateByKey(copiedKey)
        val sourcePersonId = sourceModel.findEntity(personRef).id
        val copiedPersonId = copiedModel.findEntity(personRef).id
        val copiedRelationship = copiedModel.findRelationship(relationshipRef)
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
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-rel-attrs")
        val copiedKey = ModelKey("copy-target-rel-attrs")
        val sourceRef = modelRefKey(sourceKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))
        val attributeRef = RelationshipAttributeRef.ByKey(AttributeKey("startedAt"))

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, AttributeKey("id"), typeRef("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, AttributeKey("id"), typeRef("String"), null, null))
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
                type = typeRef("String"),
                optional = true,
                name = LocalizedTextNotLocalized("Started at"),
                description = LocalizedMarkdownNotLocalized("Relationship start time")
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val sourceAttr = env.queries.findModelAggregate(sourceRef).findRelationshipAttributeOptional(relationshipRef, attributeRef)!!
        val copiedAttr = env.queries.findModelAggregateByKey(copiedKey).findRelationshipAttributeOptional(relationshipRef, attributeRef)!!
        assertEquals(sourceAttr.key, copiedAttr.key)
        assertEquals(sourceAttr.name, copiedAttr.name)
        assertEquals(sourceAttr.description, copiedAttr.description)
        assertEquals(sourceAttr.optional, copiedAttr.optional)
        assertEquals(sourceAttr.ownerId is AttributeOwnerId.OwnerRelationshipId, copiedAttr.ownerId is AttributeOwnerId.OwnerRelationshipId)
    }

    @Test
    fun `copy model relationship attributes point to copied types with the same type keys`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-rel-attr-type")
        val copiedKey = ModelKey("copy-target-rel-attr-type")
        val sourceRef = modelRefKey(sourceKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))
        val attributeRef = RelationshipAttributeRef.ByKey(AttributeKey("confirmed"))

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("Boolean"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, AttributeKey("id"), typeRef("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, AttributeKey("id"), typeRef("String"), null, null))
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
                type = typeRef("Boolean"),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copiedModel = env.queries.findModelAggregateByKey(copiedKey)
        val copiedAttr = copiedModel.findRelationshipAttributeOptional(relationshipRef, attributeRef)!!
        val copiedType = copiedModel.findType(copiedAttr.typeId)
        assertEquals(TypeKey("Boolean"), copiedType.key)
    }

    // ------------------------------------------------------------------------
    // Tags business rules
    // ------------------------------------------------------------------------

    @Test
    fun `copy model recreates local model tags with same keys names descriptions new ids and copied model local scope`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-local-model-tag")
        val copiedKey = ModelKey("copy-target-local-model-tag")
        val sourceRef = modelRefKey(sourceKey)
        val sourceTagKey = TagKey("local-model-tag")
        val sourceTagName = "Local model tag"
        val sourceTagDescription = "Model local tag description"

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        val sourceModelId = env.queries.findModelAggregate(sourceRef).id
        val sourceScope = ModelTagResolver.modelTagScopeRef(sourceModelId)
        val sourceTagRef = TagRef.ByKey(sourceScope, null, sourceTagKey)
        env.dispatchTag(TagAction.TagLocalCreate(sourceScope, sourceTagKey, sourceTagName, sourceTagDescription))
        val sourceTag = env.tagQueries.findTagByRef(sourceTagRef)
        env.dispatch(ModelAction.Model_AddTag(sourceRef, sourceTagRef))

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copied = env.queries.findModelAggregateByKey(copiedKey)
        assertEquals(1, copied.tags.size)
        assertNotEquals(sourceTag.id, copied.tags[0])
        val copiedTag = env.tagQueries.findTagByIdOptional(copied.tags[0])!!
        val copiedScope = ModelTagResolver.modelTagScopeRef(copied.id)
        assertEquals(sourceTag.key, copiedTag.key)
        assertEquals(sourceTag.name, copiedTag.name)
        assertEquals(sourceTag.description, copiedTag.description)
        assertEquals(copiedScope, copiedTag.scope)
    }

    @Test
    fun `copy model recreates unassigned local model tags in copied model local scope`() {
        val env = createEnv()
        val sourceKey = ModelKey("model-source")
        val copiedKey = ModelKey("model-dest")
        val sourceRef = modelRefKey(sourceKey)
        val localTagKey = TagKey("rgpd")
        val localTagName = "RGPD"
        val localTagDescription = "Personal data"

        // Arrange: create source model and a local tag in its scope, without attaching it.
        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        val sourceModelId = env.queries.findModelAggregate(sourceRef).id
        val sourceScope = ModelTagResolver.modelTagScopeRef(sourceModelId)
        val sourceTagRef = TagRef.ByKey(sourceScope, null, localTagKey)
        env.dispatchTag(TagAction.TagLocalCreate(sourceScope, localTagKey, localTagName, localTagDescription))
        val sourceLocalTag = env.tagQueries.findTagByRef(sourceTagRef)

        // Act: copy the model.
        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        // Assert: the unassigned tag must remain unassigned in the copied model aggregate.
        val copiedAggregate = env.queries.findModelAggregateByKey(copiedKey)
        assertTrue(copiedAggregate.tags.isEmpty())

        // Assert: the copied model local scope contains an equivalent recreated tag.
        val copiedScope = ModelTagResolver.modelTagScopeRef(copiedAggregate.id)
        val copiedLocalTags = env.tagQueries.findAllTagByScopeRef(copiedScope)
        val copiedLocalTag = copiedLocalTags.firstOrNull { tag -> tag.key == localTagKey }
        assertNotNull(copiedLocalTag)
        assertNotEquals(sourceLocalTag.id, copiedLocalTag.id)
        assertEquals(localTagName, copiedLocalTag.name)
        assertEquals(localTagDescription, copiedLocalTag.description)
    }

    @Test
    fun `copy model recreates local entity tags with same keys names descriptions new ids and copied model local scope`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-local-entity-tag")
        val copiedKey = ModelKey("copy-target-local-entity-tag")
        val sourceRef = modelRefKey(sourceKey)
        val entityRef = EntityRef.ByKey(EntityKey("customer"))
        val sourceTagKey = TagKey("local-entity-tag")

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, AttributeKey("id"), typeRef("String"), null, null))
        val sourceModelId = env.queries.findModelAggregate(sourceRef).id
        val sourceScope = ModelTagResolver.modelTagScopeRef(sourceModelId)
        val sourceTagRef = TagRef.ByKey(sourceScope, null, sourceTagKey)
        env.dispatchTag(TagAction.TagLocalCreate(sourceScope, sourceTagKey, "Entity local", "Entity local tag"))
        val sourceTag = env.tagQueries.findTagByRef(sourceTagRef)
        env.dispatch(ModelAction.Entity_AddTag(sourceRef, entityRef, sourceTagRef))

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copied = env.queries.findModelAggregateByKey(copiedKey).findEntity(entityRef)
        assertEquals(1, copied.tags.size)
        assertNotEquals(sourceTag.id, copied.tags[0])
        val copiedTag = env.tagQueries.findTagByIdOptional(copied.tags[0])!!
        assertEquals(sourceTag.key, copiedTag.key)
        assertEquals(sourceTag.name, copiedTag.name)
        assertEquals(sourceTag.description, copiedTag.description)
        assertEquals(ModelTagResolver.modelTagScopeRef(env.queries.findModelAggregateByKey(copiedKey).id), copiedTag.scope)
    }

    @Test
    fun `copy model recreates local entity attribute tags with same keys names descriptions new ids and copied model local scope`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-local-entity-attr-tag")
        val copiedKey = ModelKey("copy-target-local-entity-attr-tag")
        val sourceRef = modelRefKey(sourceKey)
        val entityRef = EntityRef.ByKey(EntityKey("customer"))
        val attributeRef = EntityAttributeRef.ByKey(AttributeKey("id"))
        val sourceTagKey = TagKey("local-entity-attr-tag")

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, AttributeKey("id"), typeRef("String"), null, null))
        val sourceModelId = env.queries.findModelAggregate(sourceRef).id
        val sourceScope = ModelTagResolver.modelTagScopeRef(sourceModelId)
        val sourceTagRef = TagRef.ByKey(sourceScope, null, sourceTagKey)
        env.dispatchTag(TagAction.TagLocalCreate(sourceScope, sourceTagKey, "Entity attr local", "Entity attribute local tag"))
        val sourceTag = env.tagQueries.findTagByRef(sourceTagRef)
        env.dispatch(ModelAction.EntityAttribute_AddTag(sourceRef, entityRef, attributeRef, sourceTagRef))

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copiedRef = modelRefKey(copiedKey)
        val copiedAttr = env.queries.findEntityAttribute(copiedRef, entityRef, attributeRef)
        assertEquals(1, copiedAttr.tags.size)
        assertNotEquals(sourceTag.id, copiedAttr.tags[0])
        val copiedTag = env.tagQueries.findTagByIdOptional(copiedAttr.tags[0])!!
        val copiedScope = ModelTagResolver.modelTagScopeRef(env.queries.findModelAggregate(copiedRef).id)
        assertEquals(sourceTag.key, copiedTag.key)
        assertEquals(sourceTag.name, copiedTag.name)
        assertEquals(sourceTag.description, copiedTag.description)
        assertEquals(copiedScope, copiedTag.scope)
    }

    @Test
    fun `copy model recreates local relationship tags with same keys names descriptions new ids and copied model local scope`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-local-rel-tag")
        val copiedKey = ModelKey("copy-target-local-rel-tag")
        val sourceRef = modelRefKey(sourceKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))
        val sourceTagKey = TagKey("local-rel-tag")

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, AttributeKey("id"), typeRef("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, AttributeKey("id"), typeRef("String"), null, null))
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
        val sourceModelId = env.queries.findModelAggregate(sourceRef).id
        val sourceScope = ModelTagResolver.modelTagScopeRef(sourceModelId)
        val sourceTagRef = TagRef.ByKey(sourceScope, null, sourceTagKey)
        env.dispatchTag(TagAction.TagLocalCreate(sourceScope, sourceTagKey, "Relationship local", "Relationship local tag"))
        val sourceTag = env.tagQueries.findTagByRef(sourceTagRef)
        env.dispatch(ModelAction.Relationship_AddTag(sourceRef, relationshipRef, sourceTagRef))

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copiedRel = env.queries.findModelAggregateByKey(copiedKey).findRelationship(relationshipRef)
        assertEquals(1, copiedRel.tags.size)
        assertNotEquals(sourceTag.id, copiedRel.tags[0])
        val copiedTag = env.tagQueries.findTagByIdOptional(copiedRel.tags[0])!!
        val copiedScope = ModelTagResolver.modelTagScopeRef(env.queries.findModelAggregateByKey(copiedKey).id)
        assertEquals(sourceTag.key, copiedTag.key)
        assertEquals(sourceTag.name, copiedTag.name)
        assertEquals(sourceTag.description, copiedTag.description)
        assertEquals(copiedScope, copiedTag.scope)
    }

    @Test
    fun `copy model recreates local relationship attribute tags with same keys names descriptions new ids and copied model local scope`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-local-rel-attr-tag")
        val copiedKey = ModelKey("copy-target-local-rel-attr-tag")
        val sourceRef = modelRefKey(sourceKey)
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))
        val relationshipAttributeRef = RelationshipAttributeRef.ByKey(AttributeKey("confirmed"))
        val sourceTagKey = TagKey("local-rel-attr-tag")

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("Boolean"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, AttributeKey("id"), typeRef("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, AttributeKey("id"), typeRef("String"), null, null))
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
                type = typeRef("Boolean"),
                optional = false,
                name = null,
                description = null
            )
        )
        val sourceModelId = env.queries.findModelAggregate(sourceRef).id
        val sourceScope = ModelTagResolver.modelTagScopeRef(sourceModelId)
        val sourceTagRef = TagRef.ByKey(sourceScope, null, sourceTagKey)
        env.dispatchTag(TagAction.TagLocalCreate(sourceScope, sourceTagKey, "Relationship attr local", "Relationship attribute local tag"))
        val sourceTag = env.tagQueries.findTagByRef(sourceTagRef)
        env.dispatch(ModelAction.RelationshipAttribute_AddTag(sourceRef, relationshipRef, relationshipAttributeRef, sourceTagRef))

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copiedAttr = env.queries.findModelAggregateByKey(copiedKey).findRelationshipAttributeOptional(relationshipRef, relationshipAttributeRef)!!
        assertEquals(1, copiedAttr.tags.size)
        assertNotEquals(sourceTag.id, copiedAttr.tags[0])
        val copiedTag = env.tagQueries.findTagByIdOptional(copiedAttr.tags[0])!!
        val copiedScope = ModelTagResolver.modelTagScopeRef(env.queries.findModelAggregateByKey(copiedKey).id)
        assertEquals(sourceTag.key, copiedTag.key)
        assertEquals(sourceTag.name, copiedTag.name)
        assertEquals(sourceTag.description, copiedTag.description)
        assertEquals(copiedScope, copiedTag.scope)
    }

    @Test
    fun `copy model keeps same global tag ids on model entities entity attributes relationships and relationship attributes`() {
        val env = createEnv()
        val sourceKey = ModelKey("copy-source-global-tags")
        val copiedKey = ModelKey("copy-target-global-tags")
        val sourceRef = modelRefKey(sourceKey)
        val entityRef = EntityRef.ByKey(EntityKey("customer"))
        val relationshipRef = RelationshipRef.ByKey(RelationshipKey("places"))
        val entityAttributeRef = EntityAttributeRef.ByKey(AttributeKey("id"))
        val relationshipAttributeRef = RelationshipAttributeRef.ByKey(AttributeKey("confirmed"))
        val globalTag = env.createGlobalTag("copy-global-group", "copy-global-tag")

        env.dispatch(ModelAction.Model_Create(sourceKey, LocalizedTextNotLocalized("Source"), null, ModelVersion("1.0.0")))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("String"), null, null))
        env.dispatch(ModelAction.Type_Create(sourceRef, TypeKey("Boolean"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("customer"), null, null, AttributeKey("id"), typeRef("String"), null, null))
        env.dispatch(ModelAction.Entity_Create(sourceRef, EntityKey("order"), null, null, AttributeKey("id"), typeRef("String"), null, null))
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
                type = typeRef("Boolean"),
                optional = false,
                name = null,
                description = null
            )
        )

        env.dispatch(ModelAction.Model_AddTag(sourceRef, globalTag.ref))
        env.dispatch(ModelAction.Entity_AddTag(sourceRef, entityRef, globalTag.ref))
        env.dispatch(ModelAction.EntityAttribute_AddTag(sourceRef, entityRef, entityAttributeRef, globalTag.ref))
        env.dispatch(ModelAction.Relationship_AddTag(sourceRef, relationshipRef, globalTag.ref))
        env.dispatch(ModelAction.RelationshipAttribute_AddTag(sourceRef, relationshipRef, relationshipAttributeRef, globalTag.ref))

        env.dispatch(ModelAction.Model_Copy(sourceRef, copiedKey))

        val copiedRef = modelRefKey(copiedKey)
        val copiedModel = env.queries.findModelAggregate(copiedRef)
        assertEquals(listOf(globalTag.id), copiedModel.tags)
        assertEquals(listOf(globalTag.id), copiedModel.findEntity(entityRef).tags)
        assertEquals(listOf(globalTag.id), env.queries.findEntityAttribute(copiedRef, entityRef, entityAttributeRef).tags)
        assertEquals(listOf(globalTag.id), copiedModel.findRelationship(relationshipRef).tags)
        assertEquals(
            listOf(globalTag.id),
            copiedModel.findRelationshipAttributeOptional(relationshipRef, relationshipAttributeRef)!!.tags
        )
    }

}
