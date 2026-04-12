package io.medatarun.model.internal

import io.medatarun.lang.idconv.IdConv
import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.infra.inmemory.BusinessKeyInMemory
import io.medatarun.model.infra.inmemory.EntityPrimaryKeyInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.infra.inmemory.PBKeyParticipantInMemory
import io.medatarun.model.ports.exposed.ModelCmdEnveloppe
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.model.ports.needs.ModelTagResolver.Companion.modelTagScopeRef
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagRef
import io.medatarun.type.commons.id.Id

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
            idConv = copyResult.idMaps,
            tagWriter = tagWriter
        )
        return copied
    }

    private class CopyModelSourceDestIdConv(
        private val entityIds: IdConv<EntityId>,
        private val relationshipIds: IdConv<RelationshipId>,
        private val attributeIds: IdConv<AttributeId>,
    ) : ModelSourceDestIdConv {
        override fun getDestEntityRef(sourceId: EntityId): EntityRef {
            val destId = entityIds.convert(sourceId)
            return EntityRef.ById(destId)
        }

        override fun getDestRelationshipRef(sourceId: RelationshipId): RelationshipRef {
            val destId = relationshipIds.convert(sourceId)
            return RelationshipRef.ById(destId)
        }

        override fun getDestEntityAttributeRef(sourceId: AttributeId): EntityAttributeRef {
            val destId = attributeIds.convert(sourceId)
            return EntityAttributeRef.ById(destId)
        }

        override fun getDestRelationshipAttributeRef(sourceId: AttributeId): RelationshipAttributeRef {
            val destId = attributeIds.convert(sourceId)
            return RelationshipAttributeRef.ById(destId)
        }
    }

    private data class CopyResult(
        val copied: ModelAggregate,
        val idMaps: ModelSourceDestIdConv,
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
        val entityPrimaryKeyIds = IdConv("pk") { EntityPrimaryKeyId.generate() }
        val businessKeyIds = IdConv("bk") { BusinessKeyId.generate() }

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

        val primaryKeys = model.entityPrimaryKeys.map { pk ->
            EntityPrimaryKeyInMemory.of(pk).copy(
                id = entityPrimaryKeyIds.convert(pk.id),
                entityId = entityIds.convert(pk.entityId),
                participants = pk.participants.map { PBKeyParticipantInMemory.of(it).copy(attributeId = attributeIds.convert(it.attributeId)) }
            )
        }

        val businessKeys = model.businessKeys.map { bk ->
            BusinessKeyInMemory.of(bk).copy(
                id = businessKeyIds.convert(bk.id),
                entityId = entityIds.convert(bk.entityId),
                participants = bk.participants.map { PBKeyParticipantInMemory.of(it).copy(attributeId = attributeIds.convert(it.attributeId)) }
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
                attributes = attributes,
                entityPrimaryKeys = primaryKeys,
                businessKeys = businessKeys
            )
        val idMaps = CopyModelSourceDestIdConv(
            entityIds = entityIds,
            relationshipIds = relationshipId,
            attributeIds = attributeIds
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
        idConv: ModelSourceDestIdConv,
        tagWriter: ModelTaggerImpl.ModelTagWriter,
    ) {

        val destModelRef = ModelRef.ById(copied.id)
        val sourceScopeRef = modelTagScopeRef(source.id)
        val copiedScopeRef = modelTagScopeRef(copied.id)
        val tagIdConv = IdConv("tag") { Id.generate(::TagId) }

        // Takes all tags from original model and copy them to the new one
        // Keep track of id changes tagIdConv
        val sourceScopeTags = tagResolver.findTagsByScope(sourceScopeRef)
        for (sourceScopeTag in sourceScopeTags) {
            tagResolver.create(
                cmdEnv.traceabilityRecord,
                copied.id,
                sourceScopeTag.key,
                sourceScopeTag.name,
                sourceScopeTag.description
            )
            val copiedTagId = tagResolver.resolveTagId(TagRef.ByKey(copiedScopeRef, null, sourceScopeTag.key))
            tagIdConv.register(sourceScopeTag.id, copiedTagId)
        }

        /**
         * For tags local to the source model scope, use the precomputed source->copied id mapping.
         * For all other scopes, keep the same tag id.
         *
         * All local tags are created before attachment replay, so local tags without
         * current usages are still copied to the destination scope.
         */
        fun resolveTag(sourceTagId: TagId): TagRef.ById {
            val sourceTag = tagResolver.findTagById(sourceTagId)
            return if (sourceTag.scope == sourceScopeRef) {
                TagRef.ById(tagIdConv.convert(sourceTagId))
            } else {
                TagRef.ById(sourceTagId)
            }
        }

        ModelTaggerImpl().applyAllTags(
            source = source,
            destModelRef = destModelRef,
            tagWriter = tagWriter,
            idMaps = idConv,
            resolveTag = ::resolveTag)

    }
}
