package io.medatarun.ext.modeljson.internal.v3

import io.medatarun.ext.modeljson.ModelJsonSchemas
import io.medatarun.ext.modeljson.ModelJsonSchemas.v_3_0
import io.medatarun.ext.modeljson.internal.base.JsonSerializerBaseVersion
import io.medatarun.model.domain.Attribute
import io.medatarun.model.domain.EntityAttributeRef
import io.medatarun.model.domain.ModelAggregate
import io.medatarun.model.domain.TypeRef

internal class JsonSerializerV3(
    private val baseVersion: JsonSerializerBaseVersion
) {
    fun toModelJson(model: ModelAggregate): ModelJsonV3 {
        val modelJsonV2 = ModelJsonV3(
            id = model.id.value.toString(),
            key = model.key.value,
            schema = ModelJsonSchemas.forVersion(v_3_0),
            version = model.version.value,
            name = model.name?.name,
            description = model.description?.name,
            origin = baseVersion.toModelOriginStr(model.origin),
            authority = model.authority.code,
            documentationHome = model.documentationHome?.toExternalForm(),
            types = toTypeJsonList(model),
            relationships = toRelationshipJsonList(model),
            entities = model.entities.map { entity ->
                val attributesJson =
                    toAttributeJsonList(model, model.attributes.filter { it.ownedBy(entity.id) })
                ModelEntityJsonV3(
                    id = entity.id.value.toString(),
                    key = entity.key.value,
                    name = entity.name?.name,
                    description = entity.description?.name,
                    origin = baseVersion.toEntityOriginStr(entity.origin),
                    attributes = attributesJson,
                    documentationHome = entity.documentationHome?.toExternalForm(),
                    tags = entity.tags.map { it.value.toString() },
                    primaryKey = model.findEntityPrimaryKeyOptional(entity.id)
                        ?.participants
                        ?.sortedBy { it.position }
                        ?.map { it.attributeId.asString() }
                        ?: emptyList()
                )
            },
            businessKeys = model.businessKeys.map { bk ->
                val e = model.findEntity(bk.entityId)
                BusinessKeyJsonV3(
                    id = bk.id.asString(),
                    key = bk.key.value,
                    entity = "key:"+e.key.asString(),
                    participants = bk.participants.sortedBy { it.position }.map {
                        val attr = model.findEntityAttribute(e.ref, EntityAttributeRef.ById(it.attributeId))
                        "key:" + attr.key.asString() },
                    name = bk.name,
                    description = bk.description
                )
            },
            tags = baseVersion.toTagList(model)
        )
        return modelJsonV2
    }

    fun toTypeJsonList(model: ModelAggregate): List<ModelTypeJsonV3> {
        return model.types.map { type ->
            ModelTypeJsonV3(
                id = type.id.value.toString(),
                key = type.key.value,
                name = type.name?.name,
                description = type.description?.name,
            )
        }
    }
    fun toAttributeJsonList(model: ModelAggregate, attrs: Collection<Attribute>): List<ModelAttributeJsonV3> {
        return attrs.map { it ->
            ModelAttributeJsonV3(
                id = it.id.value.toString(),
                key = it.key.value,
                name = it.name?.name,
                description = it.description?.name,
                type = "key:"+model.findType(TypeRef.ById(it.typeId)).key.value,
                optional = it.optional,
                tags = it.tags.map { it.value.toString() }
            )
        }
    }
    fun toRelationshipJsonList(model: ModelAggregate): List<RelationshipJsonV3> {
        return model.relationships.map { rel ->
            RelationshipJsonV3(
                id = rel.id.value.toString(),
                key = rel.key.value,
                name = rel.name?.name,
                description = rel.description?.name,
                roles = rel.roles.map { role ->
                    RelationshipRoleJsonV3(
                        id = role.id.value.toString(),
                        key = role.key.value,
                        entity = "key:"+model.findEntity(role.entityId).key.value,
                        name = role.name?.name,
                        cardinality = role.cardinality.code
                    )
                },
                attributes = toAttributeJsonList(model, model.attributes.filter { it.ownedBy(rel.id) }),
                tags = rel.tags.map { it.value.toString() }

            )
        }
    }

}