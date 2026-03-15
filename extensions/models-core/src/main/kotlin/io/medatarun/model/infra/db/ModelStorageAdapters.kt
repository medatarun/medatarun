package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.tags.core.domain.TagId
import java.net.URI

object ModelStorageAdapters {

    fun toModel(record: ModelRecord): ModelInMemory {
        return ModelInMemory(
            id = record.id,
            key = record.key,
            name = record.name,
            description = record.description,
            version = ModelVersion(record.version),
            origin = record.origin,
            authority = record.authority,
            documentationHome = record.documentationHome?.let { URI(it).toURL() },
        )
    }

    fun toType(record: ModelTypeRecord): ModelTypeInMemory = ModelTypeInMemory(
        id = record.id,
        key = record.key,
        name = record.name,
        description = record.description
    )

    fun toEntity(record: EntityRecord, tags: List<TagId>): EntityInMemory {
        val entityId = record.id
        val identifierAttributeIdString = record.identifierAttributeId

        return EntityInMemory(
            id = entityId,
            key = record.key,
            name = record.name,
            description = record.description,
            identifierAttributeId = identifierAttributeIdString,
            origin = record.origin,
            documentationHome = record.documentationHome?.let { URI(it).toURL() },
            tags = tags
        )
    }

    fun toEntityAttribute(record: EntityAttributeRecord, tags: List<TagId>): AttributeInMemory {
        return AttributeInMemory(
            id = record.id,
            key = record.key,
            name = record.name,
            description = record.description,
            typeId = record.typeId,
            optional = record.optional,
            tags = tags,
            ownerId = AttributeOwnerId.OwnerEntityId(record.entityId)
        )
    }

    fun toRelationship(
        record: RelationshipRecord,
        roles: List<RelationshipRoleRecord>,
        tags: List<TagId>
    ): RelationshipInMemory {
        return RelationshipInMemory(
            id = record.id,
            key = record.key,
            name = record.name,
            description = record.description,
            roles = roles.map { toRelationshipRole(it) },
            tags = tags
        )
    }


    fun toRelationshipRole(record: RelationshipRoleRecord): RelationshipRoleInMemory {

        return RelationshipRoleInMemory(
            id = record.id,
            key = record.key,
            entityId = record.entityId,
            name = record.name,
            cardinality = RelationshipCardinality.valueOfCode(record.cardinality)
        )
    }

    fun toRelationshipAttribute(
        record: RelationshipAttributeRecord,
        tags: List<TagId>
    ): AttributeInMemory {
        return AttributeInMemory(
            id = record.id,
            key = record.key,
            name = record.name,
            description = record.description,
            typeId = record.typeId,
            optional = record.optional,
            tags = tags,
            ownerId = AttributeOwnerId.OwnerRelationshipId(record.relationshipId)
        )
    }

}
