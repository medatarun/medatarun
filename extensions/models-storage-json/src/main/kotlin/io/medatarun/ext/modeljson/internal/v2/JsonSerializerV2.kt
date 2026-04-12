package io.medatarun.ext.modeljson.internal.v2

import io.medatarun.ext.modeljson.ModelJsonSchemas
import io.medatarun.ext.modeljson.ModelJsonSchemas.v_2_0
import io.medatarun.ext.modeljson.internal.ModelJsonWriterEntityPKIncompatibleException
import io.medatarun.ext.modeljson.internal.base.JsonSerializerBaseVersion
import io.medatarun.model.domain.EntityAttributeRef
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
            documentationHome = model.documentationHome?.toExternalForm(),
            types = baseVersion.toTypeJsonList(model),
            relationships = baseVersion.toRelationshipJsonList(model),
            entities = model.entities.map { entity ->
                val attributesJson = baseVersion.toAttributeJsonList(
                    model,
                    model.attributes.filter { it.ownedBy(entity.id) }
                )
                val attributeKey = model.entityPrimaryKeys.firstOrNull { pk -> pk.entityId == entity.id }
                    ?.participants
                    ?.firstOrNull()
                    ?.attributeId
                    ?.let { model.findEntityAttribute(entity.ref, EntityAttributeRef.ById(it)) }
                    ?.key
                    ?: throw ModelJsonWriterEntityPKIncompatibleException(
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
            tags = baseVersion.toTagList(model)
        )
        return modelJsonV2
    }

}