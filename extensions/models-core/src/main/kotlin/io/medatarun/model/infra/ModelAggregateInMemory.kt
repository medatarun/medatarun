package io.medatarun.model.infra

import io.medatarun.model.domain.*
import io.medatarun.model.infra.inmemory.BusinessKeyInMemory
import io.medatarun.model.infra.inmemory.EntityPrimaryKeyInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.infra.inmemory.PBKeyParticipantInMemory
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.id.Id
import java.net.URL

/**
 * Default implementation of Model
 */
data class ModelAggregateInMemory(
    override val model: ModelInMemory,
    override val types: List<ModelTypeInMemory>,
    override val entities: List<EntityInMemory>,
    override val attributes: List<AttributeInMemory>,
    override val relationships: List<RelationshipInMemory>,
    override val tags: List<TagId>,
    override val entityPrimaryKeys: List<EntityPrimaryKeyInMemory>,
    override val businessKeys: List<BusinessKeyInMemory>,
) : ModelAggregate, Model by model {

    companion object {
        fun of(other: ModelAggregate): ModelAggregateInMemory {
            return ModelAggregateInMemory(
                model = ModelInMemory.of(other),
                types = other.types.map(ModelTypeInMemory::of),
                entities = other.entities.map(EntityInMemory::of),
                relationships = other.relationships.map(RelationshipInMemory::of),
                tags = other.tags,
                attributes = other.attributes.map(AttributeInMemory::of),
                entityPrimaryKeys = other.entityPrimaryKeys.map(EntityPrimaryKeyInMemory::of),
                businessKeys = other.businessKeys.map(BusinessKeyInMemory::of)
            )
        }

        class Builder(
            var id: ModelId = ModelId.generate(),
            val key: ModelKey,
            var name: LocalizedText? = null,
            var description: LocalizedMarkdown? = null,
            val version: ModelVersion,
            var origin: ModelOrigin = ModelOrigin.Manual,
            var types: MutableList<ModelTypeInMemory> = mutableListOf(),
            var entities: MutableList<EntityInMemory> = mutableListOf(),
            var relationships: MutableList<RelationshipInMemory> = mutableListOf(),
            var documentationHome: URL? = null,
            var tags: MutableList<TagId> = mutableListOf(),
            var authority: ModelAuthority = ModelAuthority.SYSTEM,
            var attributes: MutableList<AttributeInMemory> = mutableListOf(),
            var entityPrimaryKeys: MutableList<EntityPrimaryKeyInMemory> = mutableListOf(),
            var businessKeys: MutableList<BusinessKeyInMemory> = mutableListOf(),
        ) {
            fun build(): ModelAggregateInMemory {
                return ModelAggregateInMemory(
                    model = ModelInMemory(
                        id = id,
                        key = key,
                        name = name,
                        description = description,
                        version = version,
                        origin = origin,
                        authority = authority,
                        documentationHome = documentationHome,
                    ),
                    types = types,
                    entities = entities,
                    relationships = relationships,
                    attributes = attributes,
                    tags = tags,
                    entityPrimaryKeys = entityPrimaryKeys,
                    businessKeys = businessKeys
                )
            }


            fun addType(type: ModelTypeInMemory): ModelTypeInMemory {
                types.add(type)
                return type
            }

            fun addType(
                key: TypeKey,
                id: TypeId = Id.generate(::TypeId),
                name: LocalizedText? = null,
                description: LocalizedMarkdown? = null
            ): ModelTypeInMemory {
                val type = ModelTypeInMemory(id, key, name, description)
                types.add(type)
                return type
            }

            fun addEntity(entity: EntityInMemory): EntityInMemory {
                entities.add(entity)
                return entity
            }

            fun addEntity(
                id: EntityId = EntityId.generate(),
                key: EntityKey,
                name: LocalizedText? = null,
                description: LocalizedMarkdown? = null,
                origin: EntityOrigin = EntityOrigin.Manual,
                documentationHome: URL? = null,
                tags: MutableList<TagId> = mutableListOf()
            ): EntityInMemory {
                val entity = EntityInMemory(
                    id = id,
                    key = key,
                    name = name,
                    description = description,
                    origin = origin,
                    documentationHome = documentationHome,
                    tags = tags
                )
                entities.add(entity)
                return entity
            }

            fun addRelationship(relationship: RelationshipInMemory): RelationshipInMemory {
                relationships.add(relationship)
                return relationship
            }

            fun addRelationship(
                id: RelationshipId = Id.generate(::RelationshipId),
                key: RelationshipKey,
                name: LocalizedText? = null,
                description: LocalizedMarkdown? = null,
                roles: List<RelationshipRoleInMemory>,
                tags: List<TagId> = emptyList(),

                ): RelationshipInMemory {
                val relationship = RelationshipInMemory(
                    id = id, key = key, name = name, description = description, roles = roles, tags = tags
                )
                relationships.add(relationship)
                return relationship
            }

            fun addAttribute(attr: AttributeInMemory) {
                this.attributes.add(attr)
            }

            fun addAttribute(
                id: AttributeId,
                ownerId: AttributeOwnerId,
                key: AttributeKey,
                name: LocalizedText? = null,
                description: LocalizedMarkdown? = null,
                typeId: TypeId,
                optional: Boolean = false,
                tags: List<TagId> = emptyList(),
            ) {
                val attr = AttributeInMemory(
                    id = id,
                    ownerId = ownerId,
                    key = key,
                    name = name,
                    description = description,
                    typeId = typeId,
                    optional = optional,
                    tags = tags
                )
                this.attributes.add(attr)
            }

            fun addTag(tagId: TagId): TagId {
                tags.add(tagId)
                return tagId
            }

            fun addEntityPrimaryKey(entityPrimaryKey: EntityPrimaryKeyInMemory): EntityPrimaryKeyInMemory {
                entityPrimaryKeys.add(entityPrimaryKey)
                return entityPrimaryKey
            }

            fun addEntityPrimaryKey(
                id: EntityPrimaryKeyId = Id.generate(::EntityPrimaryKeyId),
                entityId: EntityId,
                participants: List<PBKeyParticipantInMemory>,
            ): EntityPrimaryKeyInMemory {
                val entityPrimaryKey =
                    EntityPrimaryKeyInMemory(id = id, entityId = entityId, participants = participants)
                entityPrimaryKeys.add(entityPrimaryKey)
                return entityPrimaryKey
            }

            fun addEntityPrimaryKeySingle(
                entityId: EntityId,
                attributeId: AttributeId,
                id: EntityPrimaryKeyId = Id.generate(::EntityPrimaryKeyId),
            ): EntityPrimaryKeyInMemory {
                val entityPrimaryKey =
                    EntityPrimaryKeyInMemory(id = id, entityId = entityId, participants = listOf(PBKeyParticipantInMemory(attributeId, 0)))
                entityPrimaryKeys.add(entityPrimaryKey)
                return entityPrimaryKey
            }

            fun addBusinessKey(businessKey: BusinessKeyInMemory): BusinessKeyInMemory {
                businessKeys.add(businessKey)
                return businessKey
            }

            fun addBusinessKey(
                id: BusinessKeyId = Id.generate(::BusinessKeyId),
                key: BusinessKeyKey,
                entityId: EntityId,
                name: String? = null,
                description: String? = null,
                participants: List<PBKeyParticipantInMemory>,
            ): BusinessKeyInMemory {
                val bk = BusinessKeyInMemory(
                    id = id,
                    key = key,
                    entityId = entityId,
                    name = name,
                    description = description,
                    participants = participants
                )
                businessKeys.add(bk)
                return bk
            }
        }

        fun builder(
            key: ModelKey,
            version: ModelVersion,
            id: ModelId = ModelId.generate(),
            block: Builder.() -> Unit
        ): ModelAggregateInMemory {
            return Builder(id = id, key = key, version = version).apply(block).build()
        }

        fun builder(model: Model, block: Builder.() -> Unit = {}): ModelAggregateInMemory {
            return Builder(
                id = model.id,
                key = model.key,
                name = model.name,
                description = model.description,
                version = model.version,
                origin = model.origin,
                documentationHome = model.documentationHome,
                authority = model.authority,
            ).apply(block).build()
        }

    }
}
