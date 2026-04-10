package io.medatarun.ext.modeljson.internal.v2

import io.medatarun.ext.modeljson.internal.ModelJsonEntityIdentifierAttributeNotFound
import io.medatarun.ext.modeljson.internal.ModelJsonReadEntityReferencedInRelationshipNotFound
import io.medatarun.ext.modeljson.internal.base.JsonDeserializerBaseVersion
import io.medatarun.model.domain.*
import io.medatarun.model.domain.EntityOrigin.Uri
import io.medatarun.model.infra.*
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.infra.inmemory.BusinessKeyInMemory
import io.medatarun.model.infra.inmemory.EntityPrimaryKeyInMemory
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.id.Id
import java.net.URI

internal class JsonDeserializerV2(
    private val base: JsonDeserializerBaseVersion
) {
    fun fromJsonV2(modelJsonV2: ModelJsonV2): ModelAggregateInMemory {

        val types = modelJsonV2.types.map { typeJson ->
            ModelTypeInMemory(
                id = typeJson.id?.let { TypeId.fromString(it) } ?: TypeId.generate(),
                key = TypeKey(typeJson.key),
                name = typeJson.name,
                description = typeJson.description
            )
        }
        val attributeCollector = mutableListOf<AttributeInMemory>()
        val entityCollector = mutableListOf<EntityInMemory>()
        val relationshipCollector = mutableListOf<RelationshipInMemory>()
        val pbkCollector = mutableListOf<EntityPrimaryKeyInMemory>()

        for (entityJson in modelJsonV2.entities) {
            val entityId = entityJson.id?.let { EntityId.fromString(it) } ?: EntityId.generate()

            val attributes = base.toAttributeList(types, entityJson.attributes, AttributeOwnerId.OwnerEntityId(entityId))
            attributeCollector.addAll(attributes)

            val identifierAttribute = attributes
                .firstOrNull { it.key == AttributeKey(entityJson.identifierAttribute) }
                ?: throw ModelJsonEntityIdentifierAttributeNotFound(entityJson.key)

            val e = EntityInMemory(
                id = entityId,
                key = EntityKey(entityJson.key),
                name = entityJson.name,
                description = entityJson.description,
                identifierAttributeId = identifierAttribute.id,
                origin = when (entityJson.origin) {
                    null -> EntityOrigin.Manual
                    else -> Uri(URI(entityJson.origin))
                },
                documentationHome = entityJson.documentationHome?.let { URI(it).toURL() },
                tags = entityJson.tags?.map { Id.fromString(it, ::TagId) } ?: emptyList()
            )
            entityCollector.add(e)

            // Takes the key in JSON identifierAttribute and convert it to PBKey
            pbkCollector.add(EntityPrimaryKeyInMemory.ofSingleAttribute(entityId, identifierAttribute.id))
        }

        fun findEntity(relationJsonKey: String, roleJsonKey: String, entityKey: EntityKey) = entityCollector
            .firstOrNull { it.key == entityKey }
            ?: throw ModelJsonReadEntityReferencedInRelationshipNotFound(relationJsonKey, roleJsonKey, entityKey.value)

        for (relationJson in modelJsonV2.relationships) {
            val relationshipId = relationJson.id?.let { RelationshipId.fromString(it) } ?: RelationshipId.generate()

            val attributes = base.toAttributeList(types, relationJson.attributes, AttributeOwnerId.OwnerRelationshipId(relationshipId))
            attributeCollector.addAll(attributes)

            val r = RelationshipInMemory(
                id = relationshipId,
                key = RelationshipKey(relationJson.key),
                name = relationJson.name,
                description = relationJson.description,
                roles = relationJson.roles.map { roleJson ->
                    RelationshipRoleInMemory(
                        id = roleJson.id?.let { RelationshipRoleId.fromString(it) } ?: RelationshipRoleId.generate(),
                        key = RelationshipRoleKey(roleJson.key),
                        name = roleJson.name,
                        entityId = findEntity(relationJson.key, roleJson.key, EntityKey(roleJson.entityId)).id,
                        cardinality = RelationshipCardinality.valueOfCode(roleJson.cardinality),
                    )
                },
                tags = relationJson.tags?.map { Id.fromString(it, ::TagId) } ?: emptyList()
            )
            relationshipCollector.add(r)
        }

        val model = ModelInMemory(
            id = modelJsonV2.id?.let { ModelId.fromString(it) } ?: ModelId.generate(),
            key = ModelKey(modelJsonV2.key),
            version = ModelVersion(modelJsonV2.version),
            origin = when (modelJsonV2.origin) {
                null -> ModelOrigin.Manual
                else -> ModelOrigin.Uri(URI(modelJsonV2.origin))
            },
            authority = modelJsonV2.authority?.let { ModelAuthority.valueOfCode(it) } ?: ModelAuthority.SYSTEM,
            name = modelJsonV2.name,
            description = modelJsonV2.description,
            documentationHome = modelJsonV2.documentationHome?.let { URI(it).toURL() },
        )

        val modelAggregate = ModelAggregateInMemory(
            model = model,
            types = types,
            entities = entityCollector,
            relationships = relationshipCollector,
            attributes = attributeCollector,
            tags = modelJsonV2.tags?.map { Id.fromString(it, ::TagId) } ?: emptyList(),
            entityPrimaryKeys = pbkCollector,
            businessKeys = emptyList()
        )
        return modelAggregate
    }

}