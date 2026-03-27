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


    /**
     * Copies a model, persists it, then reproduces source tag associations on copied elements.
     */
    fun copyAndRetag(
        cmdEnv: ModelCmdEnveloppe,
        source: ModelAggregate,
        modelNewKey: ModelKey,
        persistCopied: (copied: ModelAggregate) -> Unit,
        tagWriter: ModelTaggerImpl.ModelTagWriter,
    ): ModelAggregate {
        val copyResult = copyAggregateWithIdMaps(source, modelNewKey)
        val copied = copyResult.copied
        persistCopied(copied)
        retagCopiedAggregate(
            cmdEnv = cmdEnv,
            source = source,
            copied = copied,
            idMaps = copyResult.idMaps,
            tagWriter = tagWriter
        )
        return copied
    }

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

    data class ModelSourceDestIdMaps(
        val entityIds: Map<EntityId, EntityId>,
        val relationshipIds: Map<RelationshipId, RelationshipId>,
        val attributeIds: Map<AttributeId, AttributeId>,
    )

    private data class CopyResult(
        val copied: ModelAggregate,
        val idMaps: ModelSourceDestIdMaps,
    )

    /**
     * Copies the aggregate and keeps explicit source->copied id mappings used by retagging.
     */
    private fun copyAggregateWithIdMaps(
        model: ModelAggregate,
        modelNewKey: ModelKey
    ): CopyResult {
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
        val idMaps = ModelSourceDestIdMaps(
            entityIds = entityIds.map.toMap(),
            relationshipIds = relationshipId.map.toMap(),
            attributeIds = attributeIds.map.toMap()
        )
        return CopyResult(next, idMaps)
    }

    /**
     * Applies source tags on the copied aggregate by using source->copied id maps built during copy.
     */
    private fun retagCopiedAggregate(
        cmdEnv: ModelCmdEnveloppe,
        source: ModelAggregate,
        copied: ModelAggregate,
        idMaps: ModelSourceDestIdMaps,
        tagWriter: ModelTaggerImpl.ModelTagWriter,
    ) {

        val destModelRef = ModelRef.ById(copied.id)
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

        fun applyTags(tagIds: List<TagId>, block: (tagRef: TagRef) -> Unit) {
            for(tagId in tagIds) {
                val tagRef = TagRef.ById(mapTagId(tagId))
                block(tagRef)
            }
        }

        applyTags(source.tags) { tagRef ->
            tagWriter.addModelTag(
                ModelCmd.UpdateModelTagAdd(
                    destModelRef,
                    tagRef
                )
            )
        }

        for (entity in source.entities) {
            val copiedEntityId = idMaps.entityIds[entity.id]
                ?: throw CopyModelIdConversionFailedException("entity", entity.id.toString())
            applyTags(entity.tags) { tagRef ->
                tagWriter.addEntityTag(
                    ModelCmd.UpdateEntityTagAdd(
                        destModelRef,
                        EntityRef.ById(copiedEntityId),
                        tagRef
                    )
                )
            }
        }

        for (relationship in source.relationships) {
            val copiedRelationshipId = idMaps.relationshipIds[relationship.id]
                ?: throw CopyModelIdConversionFailedException("relationship", relationship.id.toString())
            applyTags(relationship.tags) { tagRef ->
                tagWriter.addRelationshipTag(
                    ModelCmd.UpdateRelationshipTagAdd(
                        destModelRef,
                        RelationshipRef.ById(copiedRelationshipId),
                        tagRef
                    )
                )
            }
        }

        for (attribute in source.attributes) {
            val owner = attribute.ownerId
            when (owner) {
                is AttributeOwnerId.OwnerEntityId -> {
                    val copiedEntityId = idMaps.entityIds[owner.id]
                        ?: throw CopyModelIdConversionFailedException("entity", owner.id.toString())
                    val copiedAttributeId = idMaps.attributeIds[attribute.id]
                        ?: throw CopyModelIdConversionFailedException("attribute", attribute.id.toString())
                    applyTags(attribute.tags) { tagRef ->
                        tagWriter.addEntityAttributeTag(
                            ModelCmd.UpdateEntityAttributeTagAdd(
                                destModelRef,
                                EntityRef.ById(copiedEntityId),
                                EntityAttributeRef.ById(copiedAttributeId),
                                tagRef
                            )
                        )
                    }
                }

                is AttributeOwnerId.OwnerRelationshipId -> {
                    val copiedRelationshipId = idMaps.relationshipIds[owner.id]
                        ?: throw CopyModelIdConversionFailedException("relationship", owner.id.toString())
                    val copiedAttributeId = idMaps.attributeIds[attribute.id]
                        ?: throw CopyModelIdConversionFailedException("attribute", attribute.id.toString())
                    applyTags(attribute.tags) { tagRef ->
                        tagWriter.addRelationshipAttributeTag(
                            ModelCmd.UpdateRelationshipAttributeTagAdd(
                                destModelRef,
                                RelationshipRef.ById(copiedRelationshipId),
                                RelationshipAttributeRef.ById(copiedAttributeId),
                                tagRef
                            )
                        )
                    }
                }
            }
        }
    }
}
