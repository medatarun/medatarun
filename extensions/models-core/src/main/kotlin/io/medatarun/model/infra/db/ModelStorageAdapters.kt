package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.infra.AttributeInMemory
import io.medatarun.model.infra.EntityInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.infra.RelationshipInMemory
import io.medatarun.model.infra.RelationshipRoleInMemory
import io.medatarun.model.infra.db.records.EntityAttributeRecord
import io.medatarun.model.infra.db.records.EntityRecord
import io.medatarun.model.infra.db.records.ModelRecord
import io.medatarun.model.infra.db.records.ModelTypeRecord
import io.medatarun.model.infra.db.records.RelationshipAttributeRecord
import io.medatarun.model.infra.db.records.RelationshipRecord
import io.medatarun.model.infra.db.records.RelationshipRoleRecord
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.tags.core.domain.TagId
import java.net.URI

object ModelStorageAdapters {

    fun toModel(record: ModelRecord): ModelInMemory {
        val modelId = ModelId.fromString(record.id)
        return ModelInMemory(
            id = modelId,
            key = ModelKey(record.key),
            name = stringToLocalizedText(record.name),
            description = stringToLocalizedMarkdown(record.description),
            version = ModelVersion(record.version),
            origin = stringToModelOrigin(record.origin),
            documentationHome = record.documentationHome?.let { URI(it).toURL() },
        )
    }

    fun toType(record: ModelTypeRecord): ModelTypeInMemory = ModelTypeInMemory(
        id = TypeId.fromString(record.id),
        key = TypeKey(record.key),
        name = stringToLocalizedText(record.name),
        description = stringToLocalizedMarkdown(record.description)
    )

    fun toEntity(record: EntityRecord, tags: List<TagId>): EntityInMemory {
        val entityId = EntityId.fromString(record.id)
        val identifierAttributeIdString = record.identifierAttributeId

        return EntityInMemory(
            id = entityId,
            key = EntityKey(record.key),
            name = stringToLocalizedText(record.name),
            description = stringToLocalizedMarkdown(record.description),
            identifierAttributeId = AttributeId.fromString(identifierAttributeIdString),
            origin = stringToEntityOrigin(record.origin),
            documentationHome = record.documentationHome?.let { URI(it).toURL() },
            tags = tags
        )
    }

    fun toEntityAttribute(record: EntityAttributeRecord, tags: List<TagId>): AttributeInMemory {

        val attributeId = AttributeId.fromString(record.id)
        return AttributeInMemory(
            id = attributeId,
            key = AttributeKey(record.key),
            name = stringToLocalizedText(record.name),
            description = stringToLocalizedMarkdown(record.description),
            typeId = TypeId.fromString(record.typeId),
            optional = record.optional,
            tags = tags,
            ownerId = AttributeOwnerId.OwnerEntityId(EntityId.fromString(record.entityId))
        )
    }

    fun toRelationship(
        record: RelationshipRecord,
        roles: List<RelationshipRoleRecord>,
        tags: List<TagId>
    ): RelationshipInMemory {
        val relationshipId = RelationshipId.fromString(record.id)
        return RelationshipInMemory(
            id = relationshipId,
            key = RelationshipKey(record.key),
            name = stringToLocalizedText(record.name),
            description = stringToLocalizedMarkdown(record.description),
            roles = roles.map { toRelationshipRole(it) },
            tags = tags
        )
    }


    fun toRelationshipRole(record: RelationshipRoleRecord): RelationshipRoleInMemory {

        return RelationshipRoleInMemory(
            id = RelationshipRoleId.fromString(record.id),
            key = RelationshipRoleKey(record.key),
            entityId = EntityId.fromString(record.entityId),
            name = stringToLocalizedText(record.name),
            cardinality = RelationshipCardinality.valueOfCode(record.cardinality)
        )
    }

    fun toRelationshipAttribute(
        record: RelationshipAttributeRecord,
        tags: List<TagId>
    ): AttributeInMemory {
        val attributeId = AttributeId.fromString(record.id)
        return AttributeInMemory(
            id = attributeId,
            key = AttributeKey(record.key),
            name = stringToLocalizedText(record.name),
            description = stringToLocalizedMarkdown(record.description),
            typeId = TypeId.fromString(record.typeId),
            optional = record.optional,
            tags = tags,
            ownerId = AttributeOwnerId.OwnerRelationshipId(RelationshipId.fromString(record.relationshipId))
        )
    }


    private fun stringToModelOrigin(origin: String?): ModelOrigin {
        return if (origin == null) ModelOrigin.Manual else ModelOrigin.Uri(URI(origin))
    }

    private fun stringToEntityOrigin(origin: String?): EntityOrigin {
        return if (origin == null) EntityOrigin.Manual else EntityOrigin.Uri(URI(origin))
    }

    private fun stringToLocalizedText(value: String?): LocalizedText? {
        return if (value == null) null else LocalizedTextNotLocalized(value)
    }

    private fun stringToLocalizedMarkdown(value: String?): LocalizedMarkdown? {
        return if (value == null) null else LocalizedMarkdownNotLocalized(value)
    }


}