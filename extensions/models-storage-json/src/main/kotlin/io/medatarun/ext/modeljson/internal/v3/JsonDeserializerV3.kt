package io.medatarun.ext.modeljson.internal.v3

import io.medatarun.ext.modeljson.internal.ModelJsonEntityAttributeTypeNotFoundException
import io.medatarun.ext.modeljson.internal.ModelJsonReadBusinessKeyAttributeNotFoundException
import io.medatarun.ext.modeljson.internal.ModelJsonReadBusinessKeyEntityReferencedNotFoundException
import io.medatarun.ext.modeljson.internal.ModelJsonReadEntityReferencedInRelationshipNotFound
import io.medatarun.ext.modeljson.internal.base.JsonDeserializerBaseVersion
import io.medatarun.model.adapters.json.EntityAttributeRefTypeJsonConverter
import io.medatarun.model.adapters.json.EntityRefTypeJsonConverter
import io.medatarun.model.adapters.json.TypeRefTypeJsonConverter
import io.medatarun.model.domain.*
import io.medatarun.model.domain.EntityOrigin.Uri
import io.medatarun.model.infra.*
import io.medatarun.model.infra.inmemory.BusinessKeyInMemory
import io.medatarun.model.infra.inmemory.EntityPrimaryKeyInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.infra.inmemory.PBKeyParticipantInMemory
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.id.Id
import kotlinx.serialization.json.JsonPrimitive
import java.net.URI

internal class JsonDeserializerV3(
    private val base: JsonDeserializerBaseVersion
) {

    private class Collectors {
        val typeCollector: MutableList<ModelTypeInMemory> = mutableListOf()
        val attributeCollector: MutableList<AttributeInMemory> = mutableListOf()
        val entityCollector: MutableList<EntityInMemory> = mutableListOf()
        val relationshipCollector: MutableList<RelationshipInMemory> = mutableListOf()
        val pbkCollector: MutableList<EntityPrimaryKeyInMemory> = mutableListOf()
        val businessKeyCollector: MutableList<BusinessKeyInMemory> = mutableListOf()

        fun findType(ref: TypeRef): ModelTypeInMemory? {
            return typeCollector.firstOrNull { t ->
                when (ref) {
                    is TypeRef.ByKey -> t.key == ref.key
                    is TypeRef.ById -> t.id == ref.id
                }
            }
        }

        fun findEntity(entityRef: EntityRef): EntityInMemory? {
            return when (entityRef) {
                is EntityRef.ById -> entityCollector.firstOrNull { it.id == entityRef.id }
                is EntityRef.ByKey -> entityCollector.firstOrNull { it.key == entityRef.key }
            }
        }

        fun findEntityAttribute(entityRef: EntityRef, attributeRef: EntityAttributeRef): AttributeInMemory? {
            return attributeCollector.firstOrNull {
                val ownerId = it.ownerId
                val entity = findEntity(entityRef) ?: throw ModelJsonReadBusinessKeyEntityReferencedNotFoundException(entityRef.asString())
                ownerId is AttributeOwnerId.OwnerEntityId && ownerId.id == entity.id && when(attributeRef) {
                    is EntityAttributeRef.ById -> it.id == attributeRef.id
                    is EntityAttributeRef.ByKey -> it.key == attributeRef.key
                }
            }

        }
    }

    fun fromJsonV3(modelJson: ModelJsonV3): ModelAggregateInMemory {

        val collectors = Collectors()

        for (typeJson in modelJson.types ?: emptyList()) {
            val type = ModelTypeInMemory(
                id = typeJson.id?.let { TypeId.fromString(it) } ?: TypeId.generate(),
                key = TypeKey(typeJson.key),
                name = typeJson.name?.let { LocalizedTextNotLocalized(it) },
                description = typeJson.description?.let { LocalizedMarkdownNotLocalized(it) }
            )
            collectors.typeCollector.add(type)
        }


        for (entityJson in (modelJson.entities ?: emptyList())) {
            val entityId = entityJson.id?.let { EntityId.fromString(it) } ?: EntityId.generate()

            val attributes =
                toAttributeList(
                    collectors,
                    (entityJson.attributes ?: emptyList()),
                    AttributeOwnerId.OwnerEntityId(entityId)
                )
            collectors.attributeCollector.addAll(attributes)

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

            if (entityPk != null) collectors.pbkCollector.add(entityPk)

            val e = EntityInMemory(
                id = entityId,
                key = EntityKey(entityJson.key),
                name = entityJson.name?.let { LocalizedTextNotLocalized(it) },
                description = entityJson.description?.let { LocalizedMarkdownNotLocalized(it) },
                origin = when (entityJson.origin) {
                    null -> EntityOrigin.Manual
                    else -> Uri(URI(entityJson.origin))
                },
                documentationHome = entityJson.documentationHome?.let { URI(it).toURL() },
                tags = entityJson.tags?.map { Id.fromString(it, ::TagId) } ?: emptyList()
            )
            collectors.entityCollector.add(e)
        }


        for (relationJson in modelJson.relationships) {
            val relationshipId = relationJson.id?.let { RelationshipId.fromString(it) } ?: RelationshipId.generate()

            val attributes = toAttributeList(
                collectors,
                relationJson.attributes ?: emptyList(),
                AttributeOwnerId.OwnerRelationshipId(relationshipId)
            )
            collectors.attributeCollector.addAll(attributes)

            val r = RelationshipInMemory(
                id = relationshipId,
                key = RelationshipKey(relationJson.key),
                name = relationJson.name?.let { LocalizedTextNotLocalized(it) },
                description = relationJson.description?.let { LocalizedMarkdownNotLocalized(it) },
                roles = toRolesInMemory(relationJson, collectors),
                tags = relationJson.tags?.map { Id.fromString(it, ::TagId) } ?: emptyList()
            )
            collectors.relationshipCollector.add(r)
        }

        // Read business keys
        for (bkJson in (modelJson.businessKeys ?: emptyList())) {
            val bk = toBusinessKeyInMemory(collectors, bkJson)
            collectors.businessKeyCollector.add(bk)
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
            types = collectors.typeCollector,
            entities = collectors.entityCollector,
            relationships = collectors.relationshipCollector,
            attributes = collectors.attributeCollector,
            tags = modelJson.tags?.map { Id.fromString(it, ::TagId) } ?: emptyList(),
            entityPrimaryKeys = collectors.pbkCollector,
            businessKeys = collectors.businessKeyCollector
        )


        return modelAggregate
    }

    private fun toBusinessKeyInMemory(collectors: Collectors, bkJson: BusinessKeyJsonV3): BusinessKeyInMemory {
        val bkJsonId = bkJson.id
        val id: BusinessKeyId =
            bkJsonId?.let { Id.fromString(bkJsonId, ::BusinessKeyId) } ?: Id.generate(::BusinessKeyId)
        val key = BusinessKeyKey(bkJson.key).validated()

        val entityRef = EntityRefTypeJsonConverter().deserialize(JsonPrimitive(bkJson.entity))
        val entity = collectors.findEntity(entityRef)
            ?: throw ModelJsonReadBusinessKeyEntityReferencedNotFoundException(bkJson.entity)

        val participants = bkJson.participants.mapIndexed { index, string ->
            val attributeRef = EntityAttributeRefTypeJsonConverter().deserialize(JsonPrimitive(string))
            val attr = collectors.findEntityAttribute(entityRef, attributeRef)
                ?: throw ModelJsonReadBusinessKeyAttributeNotFoundException(bkJson.entity, string)
            PBKeyParticipantInMemory(attributeId = attr.id, position = index)
        }

        val bk = BusinessKeyInMemory(
            id = id,
            key = key,
            entityId = entity.id,
            name = bkJson.name?.let { LocalizedTextNotLocalized(it) },
            description = bkJson.description?.let { LocalizedMarkdownNotLocalized(it) },
            participants = participants
        )
        return bk
    }

    private fun toRolesInMemory(
        relationJson: RelationshipJsonV3,
        collectors: Collectors
    ): List<RelationshipRoleInMemory> = relationJson.roles.map { roleJson ->
        val entityRef: EntityRef = EntityRefTypeJsonConverter().deserialize(JsonPrimitive(roleJson.entity))
        val entity = collectors.findEntity(entityRef)
        val roleJsonId =
            roleJson.id?.let { RelationshipRoleId.fromString(it) } ?: RelationshipRoleId.generate()

        if (entity == null) throw ModelJsonReadEntityReferencedInRelationshipNotFound(
            relationJson.key,
            roleJsonId.asString(),
            entityRef.asString()
        )
        RelationshipRoleInMemory(
            id = roleJsonId,
            key = RelationshipRoleKey(roleJson.key),
            name = roleJson.name?.let { LocalizedTextNotLocalized(it) },
            entityId = entity.id,
            cardinality = RelationshipCardinality.valueOfCode(roleJson.cardinality),
        )
    }

    private fun toAttributeList(
        collectors: Collectors,
        attrs: Collection<ModelAttributeJsonV3>,
        ownerId: AttributeOwnerId
    ): List<AttributeInMemory> {

        return attrs.map { attributeJson ->

            val typeRef = TypeRefTypeJsonConverter().deserialize(JsonPrimitive(attributeJson.type))
            val type = collectors.findType(typeRef)
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
