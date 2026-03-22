package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.inmemory.ModelChangeEventInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.security.AppTraceabilityRecord
import io.medatarun.tags.core.domain.TagId
import kotlinx.serialization.json.Json
import java.net.URI

object ModelStorageAdapters {

    fun toModel(record: ModelRecord): ModelInMemory {
        return ModelInMemory(
            id = record.modelId,
            key = record.key,
            name = record.name,
            description = record.description,
            version = record.version,
            origin = record.origin,
            authority = record.authority,
            documentationHome = record.documentationHome?.let { URI(it).toURL() },
        )
    }

    fun toType(record: ModelTypeRecord): ModelTypeInMemory = ModelTypeInMemory(
        id = record.lineageId,
        key = record.key,
        name = record.name,
        description = record.description
    )

    fun toEntity(
        record: EntityRecord,
        tags: List<TagId>,
        identifierAttributeId: AttributeId
    ): EntityInMemory {
        return EntityInMemory(
            id = record.lineageId,
            key = record.key,
            name = record.name,
            description = record.description,
            identifierAttributeId = identifierAttributeId,
            origin = record.origin,
            documentationHome = record.documentationHome?.let { URI(it).toURL() },
            tags = tags
        )
    }

    fun toEntityAttribute(
        record: EntityAttributeRecord,
        tags: List<TagId>,
        typeId: TypeId,
        ownerEntityId: EntityId
    ): AttributeInMemory {
        return AttributeInMemory(
            id = record.lineageId,
            key = record.key,
            name = record.name,
            description = record.description,
            typeId = typeId,
            optional = record.optional,
            tags = tags,
            ownerId = AttributeOwnerId.OwnerEntityId(ownerEntityId)
        )
    }

    fun toRelationship(
        record: RelationshipRecord,
        roles: List<RelationshipRoleInMemory>,
        tags: List<TagId>
    ): RelationshipInMemory {
        return RelationshipInMemory(
            id = record.lineageId,
            key = record.key,
            name = record.name,
            description = record.description,
            roles = roles,
            tags = tags
        )
    }


    fun toRelationshipRole(
        record: RelationshipRoleRecord,
        entityId: EntityId
    ): RelationshipRoleInMemory {
        return RelationshipRoleInMemory(
            id = record.lineageId,
            key = record.key,
            entityId = entityId,
            name = record.name,
            cardinality = RelationshipCardinality.valueOfCode(record.cardinality)
        )
    }

    fun toRelationshipAttribute(
        record: RelationshipAttributeRecord,
        tags: List<TagId>,
        typeId: TypeId,
        ownerRelationshipId: RelationshipId
    ): AttributeInMemory {
        return AttributeInMemory(
            id = record.lineageId,
            key = record.key,
            name = record.name,
            description = record.description,
            typeId = typeId,
            optional = record.optional,
            tags = tags,
            ownerId = AttributeOwnerId.OwnerRelationshipId(ownerRelationshipId)
        )
    }

    fun toModelChangeEvent(record: ModelEventRecord): ModelChangeEventInMemory {
        return ModelChangeEventInMemory(
            eventId = record.id,
            eventType = record.eventType,
            eventVersion = record.eventVersion,
            eventSequenceNumber = record.streamRevision,
            createdAt = record.createdAt,
            traceabilityRecord = AppTraceabilityRecord.fromRaw(origin = record.traceabilityOrigin, actorId = record.actorId),
            modelVersion = record.modelVersion,
            payload = Json.decodeFromString(record.payload)
        )
    }

}
