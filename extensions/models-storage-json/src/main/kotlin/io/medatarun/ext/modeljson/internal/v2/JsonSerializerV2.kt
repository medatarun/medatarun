package io.medatarun.ext.modeljson.internal.v2

import io.medatarun.ext.modeljson.ModelJsonSchemas
import io.medatarun.ext.modeljson.ModelJsonSchemas.v_2_0
import io.medatarun.ext.modeljson.internal.ModelJsonWriterEntityIdentifierAttributeNotFoundInAttributes
import io.medatarun.ext.modeljson.internal.base.JsonSerializerBaseVersion
import io.medatarun.ext.modeljson.internal.base.ModelTypeJson
import io.medatarun.ext.modeljson.internal.base.RelationshipJson
import io.medatarun.ext.modeljson.internal.base.RelationshipRoleJson
import io.medatarun.model.domain.ModelAggregate

internal class JsonSerializerV2(
    private val baseVersion: JsonSerializerBaseVersion
) {
    fun toModelJsonV2(model: ModelAggregate): ModelJsonV2 {
        val modelJsonV2 = ModelJsonV2(
            id = model.id.value.toString(),
            key = model.key.value,
            schema = ModelJsonSchemas.forVersion(v_2_0),
            version = model.version.value,
            name = model.name,
            description = model.description,
            origin = baseVersion.toModelOriginStr(model.origin),
            authority = model.authority.code,
            types = model.types.map { type ->
                ModelTypeJson(
                    id = type.id.value.toString(),
                    key = type.key.value,
                    name = type.name,
                    description = type.description,
                )
            },
            relationships = model.relationships.map { rel ->
                RelationshipJson(
                    id = rel.id.value.toString(),
                    key = rel.key.value,
                    name = rel.name,
                    description = rel.description,
                    roles = rel.roles.map { role ->
                        RelationshipRoleJson(
                            id = role.id.value.toString(),
                            key = role.key.value,
                            entityId = model.findEntity(role.entityId).key.value,
                            name = role.name,
                            cardinality = role.cardinality.code
                        )
                    },
                    attributes = baseVersion.toAttributeJsonList(model, model.attributes.filter { it.ownedBy(rel.id) }),
                    tags = rel.tags.map { it.value.toString() }

                )
            },
            entities = model.entities.map { entity ->
                val attributesJson = baseVersion.toAttributeJsonList(model, model.attributes.filter { it.ownedBy(entity.id) })
                val attributeKey = model.attributes
                    .firstOrNull { attribute -> entity.identifierAttributeId == attribute.id }?.key
                    ?: throw ModelJsonWriterEntityIdentifierAttributeNotFoundInAttributes(
                        entity.id,
                        entity.identifierAttributeId
                    )
                ModelEntityJsonV2(
                    id = entity.id.value.toString(),
                    key = entity.key.value,
                    name = entity.name,
                    description = entity.description,
                    identifierAttribute = attributeKey.value,
                    origin = baseVersion.toEntityOriginStr(entity.origin),
                    attributes = attributesJson,
                    documentationHome = entity.documentationHome?.toExternalForm(),
                    tags = entity.tags.map { it.value.toString() }
                )
            },
            documentationHome = model.documentationHome?.toExternalForm(),
            tags = model.tags.map { it.value.toString() }
        )
        return modelJsonV2
    }

}