package io.medatarun.model.internal

import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmdEnveloppe
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.model.ports.needs.ModelTagResolver.Companion.modelTagScopeRef
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagRef

class ModelCmdCopyImpl(
    private val tagResolver: ModelTagResolver,
) {

    class IdConv<T>(val name: String, val factory: () -> T) {
        val map = mutableMapOf<T, T>()
        fun generate(old: T): T {
            val newId = factory()
            map[old] = newId
            return newId
        }

        fun convert(old: T): T {
            return map[old] ?: throw CopyModelIdConversionFailedException(name, old.toString())
        }
    }

    fun copy(
        model: ModelAggregate,
        modelNewKey: ModelKey
    ): ModelAggregate {
        val typeIds = IdConv("type") { TypeId.generate() }
        val entityIds = IdConv("entity") { EntityId.generate() }
        val relationshipId = IdConv("relationship") { RelationshipId.generate() }
        val attributeIds = IdConv("attribute") { AttributeId.generate() }

        // Respect the order otherwise id conversion will fail!

        val newTypes = model.types.map { type ->
            ModelTypeInMemory.of(type).copy(id = typeIds.generate(type.id))
        }
        // Build attribute id conversions before copying entities because entities reference identifier attributes.
        model.attributes.forEach { attr ->
            attributeIds.generate(attr.id)
        }
        val newEntities = model.entities.map { entity ->
            EntityInMemory.of(entity).copy(
                id = entityIds.generate(entity.id),
                identifierAttributeId = attributeIds.convert(entity.identifierAttributeId),
            )
        }
        val newRelationships = model.relationships.map { rel ->
            RelationshipInMemory.of(rel).copy(
                id = relationshipId.generate(rel.id),
                roles = rel.roles.map { role ->
                    RelationshipRoleInMemory.of(role).copy(
                        id = RelationshipRoleId.generate(),
                        entityId = entityIds.convert(role.entityId)
                    )
                }
            )
        }

        val attributes = model.attributes.map { attr ->
            val ownerId = attr.ownerId
            AttributeInMemory.of(attr).copy(
                id = attributeIds.convert(attr.id),
                typeId = typeIds.convert(attr.typeId),
                ownerId = when (ownerId) {
                    is AttributeOwnerId.OwnerEntityId -> AttributeOwnerId.OwnerEntityId(entityIds.convert(ownerId.id))
                    is AttributeOwnerId.OwnerRelationshipId -> AttributeOwnerId.OwnerRelationshipId(
                        relationshipId.convert(
                            ownerId.id
                        )
                    )
                }
            )
        }

        val next = ModelAggregateInMemory.of(model)
            .copy(
                model = ModelInMemory.of(model).copy(
                    id = ModelId.generate(),
                    key = modelNewKey,
                    // A model copy falls back to system, always (business rule) to not pollute the list of canonical models. Users can promote that manually later.
                    authority = ModelAuthority.SYSTEM,
                ),
                types = newTypes,
                entities = newEntities,
                relationships = newRelationships,
                attributes = attributes
            )
        return next
    }

    fun retag(
        cmdEnv: ModelCmdEnveloppe,
        source: ModelAggregate,
        copied: ModelAggregate,
        updateModelTagAdd: (cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateModelTagAdd) -> Unit,
        updateEntityTagAdd: (cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityTagAdd) -> Unit,
        updateRelationshipTagAdd: (cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateRelationshipTagAdd) -> Unit,
        updateEntityAttributeTagAdd: (cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityAttributeTagAdd) -> Unit,
        updateRelationshipAttributeTagAdd: (cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateRelationshipAttributeTagAdd) -> Unit,
    ) {
        val copiedModelRef = ModelRef.ById(copied.id)
        val sourceScopeRef = modelTagScopeRef(source.id)
        val copiedScopeRef = modelTagScopeRef(copied.id)
        val copiedTagIdsBySourceTagId = mutableMapOf<TagId, TagId>()

        /**
         * For tags local to the source model scope, create an equivalent local tag in the copied model scope.
         * For all other scopes, keep the same tag id.
         *
         * Uses the copy map to avoid fetching multiple times the same tag id
         */
        fun mapTagId(sourceTagId: TagId): TagId {
            val existing = copiedTagIdsBySourceTagId[sourceTagId]
            if (existing != null) return existing

            val sourceTag = tagResolver.findTagById(sourceTagId)
            val mappedTagId = if (sourceTag.scope == sourceScopeRef) {
                tagResolver.create(
                    cmdEnv.traceabilityRecord,
                    copied.id,
                    sourceTag.key,
                    sourceTag.name,
                    sourceTag.description
                )
                tagResolver.resolveTagId(TagRef.ByKey(copiedScopeRef, null, sourceTag.key))
            } else {
                sourceTagId
            }
            copiedTagIdsBySourceTagId[sourceTagId] = mappedTagId
            return mappedTagId
        }

        source.tags.forEach { sourceTagId ->
            updateModelTagAdd(
                cmdEnv,
                ModelCmd.UpdateModelTagAdd(
                    copiedModelRef,
                    TagRef.ById(mapTagId(sourceTagId))
                )
            )
        }

        source.entities.forEach { sourceEntity ->
            val copiedEntity = copied.findEntity(sourceEntity.key)
            sourceEntity.tags.forEach { sourceTagId ->
                updateEntityTagAdd(
                    cmdEnv,
                    ModelCmd.UpdateEntityTagAdd(
                        copiedModelRef,
                        EntityRef.ById(copiedEntity.id),
                        TagRef.ById(mapTagId(sourceTagId))
                    )
                )
            }
        }

        source.relationships.forEach { sourceRelationship ->
            val copiedRelationship = copied.findRelationship(RelationshipRef.ByKey(sourceRelationship.key))
            sourceRelationship.tags.forEach { sourceTagId ->
                updateRelationshipTagAdd(
                    cmdEnv,
                    ModelCmd.UpdateRelationshipTagAdd(
                        copiedModelRef,
                        RelationshipRef.ById(copiedRelationship.id),
                        TagRef.ById(mapTagId(sourceTagId))
                    )
                )
            }
        }

        source.attributes.forEach { sourceAttribute ->
            val sourceOwner = sourceAttribute.ownerId
            if (sourceOwner is AttributeOwnerId.OwnerEntityId) {
                val sourceEntity = source.findEntity(sourceOwner.id)
                val copiedEntity = copied.findEntity(sourceEntity.key)
                val copiedAttribute = copied.findEntityAttribute(
                    EntityRef.ById(copiedEntity.id),
                    EntityAttributeRef.ByKey(sourceAttribute.key)
                )
                sourceAttribute.tags.forEach { sourceTagId ->
                    updateEntityAttributeTagAdd(
                        cmdEnv,
                        ModelCmd.UpdateEntityAttributeTagAdd(
                            copiedModelRef,
                            EntityRef.ById(copiedEntity.id),
                            EntityAttributeRef.ById(copiedAttribute.id),
                            TagRef.ById(mapTagId(sourceTagId))
                        )
                    )
                }
            }
            if (sourceOwner is AttributeOwnerId.OwnerRelationshipId) {
                val sourceRelationship = source.findRelationship(sourceOwner.id)
                val copiedRelationship = copied.findRelationship(RelationshipRef.ByKey(sourceRelationship.key))
                val copiedAttribute = copied.findRelationshipAttributeOptional(
                    RelationshipRef.ById(copiedRelationship.id),
                    RelationshipAttributeRef.ByKey(sourceAttribute.key)
                ) ?: throw RelationshipAttributeNotFoundException(
                    copiedModelRef,
                    RelationshipRef.ById(copiedRelationship.id),
                    RelationshipAttributeRef.ByKey(sourceAttribute.key)
                )
                sourceAttribute.tags.forEach { sourceTagId ->
                    updateRelationshipAttributeTagAdd(
                        cmdEnv,
                        ModelCmd.UpdateRelationshipAttributeTagAdd(
                            copiedModelRef,
                            RelationshipRef.ById(copiedRelationship.id),
                            RelationshipAttributeRef.ById(copiedAttribute.id),
                            TagRef.ById(mapTagId(sourceTagId))
                        )
                    )
                }
            }
        }
    }
}
