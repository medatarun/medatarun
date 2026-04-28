package io.medatarun.ext.modeljson.internal.base

import io.medatarun.ext.modeljson.internal.ModelJsonEntityAttributeTypeNotFoundException
import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.AttributeOwnerId
import io.medatarun.model.domain.ModelType
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.infra.AttributeInMemory
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.id.Id

internal class JsonDeserializerBaseVersion {
    fun toAttributeList(
        types: List<ModelType>,
        attrs: Collection<ModelAttributeJson>,
        ownerId: AttributeOwnerId
    ): List<AttributeInMemory> {

        return attrs.map { attributeJson ->

            val type = types.firstOrNull { t -> t.key == TypeKey(attributeJson.type) }
                ?: throw ModelJsonEntityAttributeTypeNotFoundException(attributeJson.key, attributeJson.type)

            AttributeInMemory(
                id = attributeJson.id?.let { AttributeId.fromString(it) } ?: AttributeId.generate(),
                key = AttributeKey(attributeJson.key),
                name = attributeJson.name?.toTextSingleLine(),
                description = attributeJson.description?.toTextMarkdown(),
                optional = attributeJson.optional,
                typeId = type.id,
                tags = attributeJson.tags?.map { Id.fromString(it, ::TagId) } ?: emptyList(),
                ownerId = ownerId
            )
        }
    }

}