package io.medatarun.ext.modeljson.internal.v3

import io.medatarun.ext.modeljson.ModelJsonSchemas
import io.medatarun.ext.modeljson.ModelJsonSchemas.v_2_0
import io.medatarun.ext.modeljson.ModelJsonSchemas.v_3_0
import io.medatarun.ext.modeljson.internal.base.JsonSerializerBaseVersion
import io.medatarun.model.domain.ModelAggregate

internal class JsonSerializerV3(
    private val baseVersion: JsonSerializerBaseVersion
) {
    fun toModelJson(model: ModelAggregate): ModelJsonV3 {
        val modelJsonV2 = ModelJsonV3(
            id = model.id.value.toString(),
            key = model.key.value,
            schema = ModelJsonSchemas.forVersion(v_3_0),
            version = model.version.value,
            name = model.name,
            description = model.description,
            origin = baseVersion.toModelOriginStr(model.origin),
            authority = model.authority.code,
            documentationHome = model.documentationHome?.toExternalForm(),
            types = baseVersion.toTypeJsonList(model),
            relationships = baseVersion.toRelationshipJsonList(model),
            entities = model.entities.map { entity ->
                val attributesJson =
                    baseVersion.toAttributeJsonList(model, model.attributes.filter { it.ownedBy(entity.id) })
                ModelEntityJsonV3(
                    id = entity.id.value.toString(),
                    key = entity.key.value,
                    name = entity.name,
                    description = entity.description,
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
                BusinessKeyJsonV3(
                    id = bk.id.asString(),
                    key = bk.key.value,
                    entityId = bk.entityId.asString(),
                    participants = bk.participants.sortedBy { it.position }.map { it.attributeId.asString() },
                    name = bk.name,
                    description = bk.description
                )
            },
            tags = baseVersion.toTagList(model)
        )
        return modelJsonV2
    }

}