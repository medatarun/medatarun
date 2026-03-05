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
            name = stringToLocalizedText(record.name),
            description = stringToLocalizedMarkdown(record.description),
            version = ModelVersion(record.version),
            origin = stringToModelOrigin(record.origin),
            documentationHome = record.documentationHome?.let { URI(it).toURL() },
        )
    }

    fun toType(record: ModelTypeRecord): ModelTypeInMemory = ModelTypeInMemory(
        id = record.id,
        key = record.key,
        name = stringToLocalizedText(record.name),
        description = stringToLocalizedMarkdown(record.description)
    )

    fun toEntity(record: EntityRecord, tags: List<TagId>): EntityInMemory {
        val entityId = record.id
        val identifierAttributeIdString = record.identifierAttributeId

        return EntityInMemory(
            id = entityId,
            key = record.key,
            name = stringToLocalizedText(record.name),
            description = stringToLocalizedMarkdown(record.description),
            identifierAttributeId = identifierAttributeIdString,
            origin = stringToEntityOrigin(record.origin),
            documentationHome = record.documentationHome?.let { URI(it).toURL() },
            tags = tags
        )
    }

    fun toEntityAttribute(record: EntityAttributeRecord, tags: List<TagId>): AttributeInMemory {
        return AttributeInMemory(
            id = record.id,
            key = record.key,
            name = stringToLocalizedText(record.name),
            description = stringToLocalizedMarkdown(record.description),
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
            name = stringToLocalizedText(record.name),
            description = stringToLocalizedMarkdown(record.description),
            roles = roles.map { toRelationshipRole(it) },
            tags = tags
        )
    }


    fun toRelationshipRole(record: RelationshipRoleRecord): RelationshipRoleInMemory {

        return RelationshipRoleInMemory(
            id = record.id,
            key = record.key,
            entityId = record.entityId,
            name = stringToLocalizedText(record.name),
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
            name = stringToLocalizedText(record.name),
            description = stringToLocalizedMarkdown(record.description),
            typeId = record.typeId,
            optional = record.optional,
            tags = tags,
            ownerId = AttributeOwnerId.OwnerRelationshipId(record.relationshipId)
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