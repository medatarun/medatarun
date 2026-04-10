package io.medatarun.ext.modeljson.internal.v3

import io.medatarun.ext.modeljson.internal.ModelJsonEntityAttributeTypeNotFoundException
import io.medatarun.ext.modeljson.internal.ModelJsonReadEntityReferencedInRelationshipNotFound
import io.medatarun.ext.modeljson.internal.base.JsonDeserializerBaseVersion
import io.medatarun.model.domain.*
import io.medatarun.model.domain.EntityOrigin.Uri
import io.medatarun.model.infra.*
import io.medatarun.model.infra.inmemory.BusinessKeyInMemory
import io.medatarun.model.infra.inmemory.EntityPrimaryKeyInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.infra.inmemory.PBKeyParticipantInMemory
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.id.Id
import java.net.URI

internal class JsonDeserializerV3(
    private val base: JsonDeserializerBaseVersion
) {
    fun fromJsonV3(modelJson: ModelJsonV3): ModelAggregateInMemory {

        val types = (modelJson.types ?: emptyList()).map { typeJson ->
            ModelTypeInMemory(
                id = typeJson.id?.let { TypeId.fromString(it) } ?: TypeId.generate(),
                key = TypeKey(typeJson.key),
                name = typeJson.name?.let { LocalizedTextNotLocalized(it) },
                description = typeJson.description?.let { LocalizedMarkdownNotLocalized(it) }
            )
        }
        val attributeCollector = mutableListOf<AttributeInMemory>()
        val entityCollector = mutableListOf<EntityInMemory>()
        val relationshipCollector = mutableListOf<RelationshipInMemory>()
        val pbkCollector = mutableListOf<EntityPrimaryKeyInMemory>()
        val businessKeyCollector = mutableListOf<BusinessKeyInMemory>()


        for (entityJson in (modelJson.entities ?: emptyList())) {
            val entityId = entityJson.id?.let { EntityId.fromString(it) } ?: EntityId.generate()

            val attributes =
                toAttributeList(types, (entityJson.attributes ?: emptyList()), AttributeOwnerId.OwnerEntityId(entityId))
            attributeCollector.addAll(attributes)

            // Read primary key attributes
            val pk = entityJson.primaryKey ?: emptyList()
            val entityPk = if (pk.isNotEmpty()) {
                EntityPrimaryKeyInMemory(
                    id = Id.generate(::EntityPrimaryKeyId),
                    entityId = entityId,
                    participants = pk.mapIndexed { index, string ->
                        PBKeyParticipantInMemory(attributeId = Id.fromString(string, ::AttributeId), position = index)
                    }
                )
            } else null

            if (entityPk != null) pbkCollector.add(entityPk)


            // TODO on sait ici que c'est faux mais c'est le temps de terminer le chantier complet
            val identifierAttribute = entityPk?.participants?.firstOrNull()?.attributeId
                ?: Id.generate(::AttributeId)


            val e = EntityInMemory(
                id = entityId,
                key = EntityKey(entityJson.key),
                name = entityJson.name?.let { LocalizedTextNotLocalized(it) },
                description = entityJson.description?.let { LocalizedMarkdownNotLocalized(it) },
                // TODO on sait ici que c'est faux mais c'est le temps de terminer le chantier complet
                identifierAttributeId = identifierAttribute,
                origin = when (entityJson.origin) {
                    null -> EntityOrigin.Manual
                    else -> Uri(URI(entityJson.origin))
                },
                documentationHome = entityJson.documentationHome?.let { URI(it).toURL() },
                tags = entityJson.tags?.map { Id.fromString(it, ::TagId) } ?: emptyList()
            )
            entityCollector.add(e)
        }

        fun findEntity(relationJsonKey: String, roleJsonKey: String, entityKey: EntityKey) = entityCollector
            .firstOrNull { it.key == entityKey }
            ?: throw ModelJsonReadEntityReferencedInRelationshipNotFound(relationJsonKey, roleJsonKey, entityKey.value)

        for (relationJson in modelJson.relationships) {
            val relationshipId = relationJson.id?.let { RelationshipId.fromString(it) } ?: RelationshipId.generate()

            val attributes = toAttributeList(
                types,
                relationJson.attributes ?: emptyList(),
                AttributeOwnerId.OwnerRelationshipId(relationshipId)
            )
            attributeCollector.addAll(attributes)

            val r = RelationshipInMemory(
                id = relationshipId,
                key = RelationshipKey(relationJson.key),
                name = relationJson.name?.let { LocalizedTextNotLocalized(it) },
                description = relationJson.description?.let { LocalizedMarkdownNotLocalized(it) },
                roles = relationJson.roles.map { roleJson ->
                    RelationshipRoleInMemory(
                        id = roleJson.id?.let { RelationshipRoleId.fromString(it) } ?: RelationshipRoleId.generate(),
                        key = RelationshipRoleKey(roleJson.key),
                        name = roleJson.name?.let { LocalizedTextNotLocalized(it) },
                        entityId = findEntity(relationJson.key, roleJson.key, EntityKey(roleJson.entityId)).id,
                        cardinality = RelationshipCardinality.valueOfCode(roleJson.cardinality),
                    )
                },
                tags = relationJson.tags?.map { Id.fromString(it, ::TagId) } ?: emptyList()
            )
            relationshipCollector.add(r)
        }

        // Read business keys
        for (bkJson in (modelJson.businessKeys ?: emptyList())) {
            val bkJsonId = bkJson.id
            val id: BusinessKeyId =
                bkJsonId?.let { Id.fromString(bkJsonId, ::BusinessKeyId) } ?: Id.generate(::BusinessKeyId)
            val bk = BusinessKeyInMemory(
                id = id,
                key = BusinessKeyKey(bkJson.key).validated(),
                entityId = Id.fromString(bkJson.entityId, ::EntityId),
                name = bkJson.name,
                description = bkJson.description,
                participants = bkJson.participants.mapIndexed { index, string ->
                    PBKeyParticipantInMemory(
                        attributeId = Id.fromString(
                            string,
                            ::AttributeId
                        ), position = index
                    )
                }
            )
            businessKeyCollector.add(bk)
        }

        val model = ModelInMemory(
            id = modelJson.id?.let { ModelId.fromString(it) } ?: ModelId.generate(),
            key = ModelKey(modelJson.key),
            version = ModelVersion(modelJson.version),
            origin = when (modelJson.origin) {
                null -> ModelOrigin.Manual
                else -> ModelOrigin.Uri(URI(modelJson.origin))
            },
            authority = modelJson.authority?.let { ModelAuthority.valueOfCode(it) } ?: ModelAuthority.SYSTEM,
            name = modelJson.name?.let { LocalizedTextNotLocalized(it) },
            description = modelJson.description?.let { LocalizedMarkdownNotLocalized(it) },
            documentationHome = modelJson.documentationHome?.let { URI(it).toURL() },
        )

        val modelAggregate = ModelAggregateInMemory(
            model = model,
            types = types,
            entities = entityCollector,
            relationships = relationshipCollector,
            attributes = attributeCollector,
            tags = modelJson.tags?.map { Id.fromString(it, ::TagId) } ?: emptyList(),
            entityPrimaryKeys = pbkCollector,
            businessKeys = businessKeyCollector
        )


        return modelAggregate
    }

    fun toAttributeList(
        types: List<ModelType>,
        attrs: Collection<ModelAttributeJsonV3>,
        ownerId: AttributeOwnerId
    ): List<AttributeInMemory> {

        return attrs.map { attributeJson ->

            val type = types.firstOrNull { t -> t.key == TypeKey(attributeJson.type) }
                ?: throw ModelJsonEntityAttributeTypeNotFoundException(attributeJson.key, attributeJson.type)

            AttributeInMemory(
                id = attributeJson.id?.let { AttributeId.fromString(it) } ?: AttributeId.generate(),
                key = AttributeKey(attributeJson.key),
                name = attributeJson.name?.let { LocalizedTextNotLocalized(it) },
                description = attributeJson.description?.let { LocalizedMarkdownNotLocalized(it) },
                optional = attributeJson.optional,
                typeId = type.id,
                tags = attributeJson.tags?.map { Id.fromString(it, ::TagId) } ?: emptyList(),
                ownerId = ownerId
            )
        }
    }

}