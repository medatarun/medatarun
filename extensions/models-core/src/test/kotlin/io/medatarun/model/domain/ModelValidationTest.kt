package io.medatarun.model.domain

import io.medatarun.model.infra.AttributeInMemory
import io.medatarun.model.infra.ModelAggregateInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.infra.RelationshipInMemory
import io.medatarun.model.infra.RelationshipRoleInMemory
import io.medatarun.model.internal.ModelValidationImpl
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test
import kotlin.test.assertIs

class ModelValidationTest {
    private val validation: ModelValidation = ModelValidationImpl()

    @Test
    fun `model with bad entity identifier`() {
        val model = ModelAggregateInMemory.builder(
            key = ModelKey("test"),
            version = ModelVersion("0.0.1"),
        ) {
            val typeStringId = TypeId.generate()
            types = mutableListOf(
                ModelTypeInMemory(
                    id = typeStringId,
                    key = TypeKey("String"),
                    name = null,
                    description = null
                )
            )
            val goodAttributeId = AttributeId.generate()
            val badAttributeId = AttributeId.generate()
            val e = addEntityWithIdentifierAttributeId(
                key = EntityKey("Contact"),
                identifierAttributeId = badAttributeId
            ) {

            }
            addAttribute(
                AttributeInMemory(
                    id = goodAttributeId,
                    key = AttributeKey("id"),
                    typeId = typeStringId,
                    name = null,
                    description = null,
                    optional = false,
                    tags = emptyList(),
                    ownerId = AttributeOwnerId.OwnerEntityId(e.id)
                )
            )


        }
        val result = validation.validate(model)
        assertIs<ModelValidationState.Error>(result)
        kotlin.test.assertEquals(1, result.errors.size)
        assertIs<ModelValidationErrorInvalidIdentityAttribute>(result.errors.first())

    }

    @Test
    fun `model with bad attribute type`() {
        val identifierAttribute = AttributeId.generate()
        val typeIdString = TypeId.generate()
        val typeIdInvalid = TypeId.generate()
        val model = ModelAggregateInMemory.builder(
            key = ModelKey("test"),
            version = ModelVersion("0.0.1"),
        ) {
            name = null
            description = null
            types = mutableListOf(
                ModelTypeInMemory(
                    id = typeIdString,
                    key = TypeKey("String"),
                    name = null,
                    description = null
                )
            )
            val contact = addEntityWithIdentifierAttributeId(
                key = EntityKey("Contact"),
                identifierAttributeId = identifierAttribute,
            ) {

            }
            addAttribute(
                AttributeInMemory(
                    id = identifierAttribute,
                    key = AttributeKey("id"),
                    typeId = typeIdInvalid,
                    name = null,
                    description = null,
                    optional = false,
                    tags = emptyList(),
                    ownerId = AttributeOwnerId.OwnerEntityId(contact.id)
                )
            )
        }

        val result = validation.validate(model)
        assertIs<ModelValidationState.Error>(result)
        assertEquals(1, result.errors.size)
        assertIs<ModelValidationErrorTypeNotFound>(result.errors.first())
    }

    @Test
    fun `model with duplicate keys`() {
        val typeId = TypeId.generate()
        val entityOneIdentifier = AttributeId.generate()
        val entityTwoIdentifier = AttributeId.generate()
        val relationshipId = RelationshipId.generate()
        val relationshipAttrOne = AttributeId.generate()
        val relationshipAttrTwo = AttributeId.generate()
        val model = ModelAggregateInMemory.builder(
            key = ModelKey("test"),
            version = ModelVersion("0.0.1"),
        ) {
            types = mutableListOf(
                ModelTypeInMemory(id = typeId, key = TypeKey("String"), name = null, description = null),
                ModelTypeInMemory(id = TypeId.generate(), key = TypeKey("String"), name = null, description = null),
            )

            val duplicateEntityKey = EntityKey("Contact")
            val entityOne = addEntityWithIdentifierAttributeId(
                key = duplicateEntityKey,
                identifierAttributeId = entityOneIdentifier,
            ) {}
            val entityTwo = addEntityWithIdentifierAttributeId(
                key = duplicateEntityKey,
                identifierAttributeId = entityTwoIdentifier,
            ) {}

            addAttribute(
                AttributeInMemory(
                    id = entityOneIdentifier,
                    key = AttributeKey("id"),
                    typeId = typeId,
                    name = null,
                    description = null,
                    optional = false,
                    tags = emptyList(),
                    ownerId = AttributeOwnerId.OwnerEntityId(entityOne.id)
                )
            )
            addAttribute(
                AttributeInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("id"),
                    typeId = typeId,
                    name = null,
                    description = null,
                    optional = false,
                    tags = emptyList(),
                    ownerId = AttributeOwnerId.OwnerEntityId(entityOne.id)
                )
            )
            addAttribute(
                AttributeInMemory(
                    id = entityTwoIdentifier,
                    key = AttributeKey("code"),
                    typeId = typeId,
                    name = null,
                    description = null,
                    optional = false,
                    tags = emptyList(),
                    ownerId = AttributeOwnerId.OwnerEntityId(entityTwo.id)
                )
            )

            val duplicateRelationshipKey = RelationshipKey("Manages")
            relationships = mutableListOf(
                RelationshipInMemory(
                    id = relationshipId,
                    key = duplicateRelationshipKey,
                    name = null,
                    description = null,
                    roles = listOf(
                        RelationshipRoleInMemory(
                            id = RelationshipRoleId.generate(),
                            key = RelationshipRoleKey("manager"),
                            entityId = entityOne.id,
                            name = null,
                            cardinality = RelationshipCardinality.One
                        )
                    ),
                    tags = emptyList()
                ),
                RelationshipInMemory(
                    id = RelationshipId.generate(),
                    key = duplicateRelationshipKey,
                    name = null,
                    description = null,
                    roles = listOf(
                        RelationshipRoleInMemory(
                            id = RelationshipRoleId.generate(),
                            key = RelationshipRoleKey("target"),
                            entityId = entityTwo.id,
                            name = null,
                            cardinality = RelationshipCardinality.Many
                        )
                    ),
                    tags = emptyList()
                )
            )

            addAttribute(
                AttributeInMemory(
                    id = relationshipAttrOne,
                    key = AttributeKey("since"),
                    typeId = typeId,
                    name = null,
                    description = null,
                    optional = false,
                    tags = emptyList(),
                    ownerId = AttributeOwnerId.OwnerRelationshipId(relationshipId)
                )
            )
            addAttribute(
                AttributeInMemory(
                    id = relationshipAttrTwo,
                    key = AttributeKey("since"),
                    typeId = typeId,
                    name = null,
                    description = null,
                    optional = true,
                    tags = emptyList(),
                    ownerId = AttributeOwnerId.OwnerRelationshipId(relationshipId)
                )
            )
        }

        val result = validation.validate(model)
        assertIs<ModelValidationState.Error>(result)
        assertEquals(5, result.errors.size)
        kotlin.test.assertTrue(result.errors.any { it is ModelValidationErrorDuplicateTypeKey })
        kotlin.test.assertTrue(result.errors.any { it is ModelValidationErrorDuplicateEntityKey })
        kotlin.test.assertTrue(result.errors.any { it is ModelValidationErrorDuplicateEntityAttributeKey })
        kotlin.test.assertTrue(result.errors.any { it is ModelValidationErrorDuplicateRelationshipKey })
        kotlin.test.assertTrue(result.errors.any { it is ModelValidationErrorDuplicateRelationshipAttributeKey })
    }

}
