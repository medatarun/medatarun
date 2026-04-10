package io.medatarun.ext.modeljson.internal.base

import io.medatarun.model.domain.Attribute
import io.medatarun.model.domain.EntityOrigin
import io.medatarun.model.domain.ModelAggregate
import io.medatarun.model.domain.ModelOrigin
import io.medatarun.model.domain.TypeRef

internal class JsonSerializerBaseVersion {
    fun toEntityOriginStr(origin: EntityOrigin): String? {
        val originStr = when (origin) {
            is EntityOrigin.Manual -> null
            is EntityOrigin.Uri -> origin.uri.toString()
        }
        return originStr
    }

    fun toModelOriginStr(origin: ModelOrigin): String? {
        val originStr = when (origin) {
            is ModelOrigin.Manual -> null
            is ModelOrigin.Uri -> origin.uri.toString()
        }
        return originStr
    }

    fun toAttributeJsonList(model: ModelAggregate, attrs: Collection<Attribute>): List<ModelAttributeJson> {
        return attrs.map { it ->
            ModelAttributeJson(
                id = it.id.value.toString(),
                key = it.key.value,
                name = it.name,
                description = it.description,
                type = model.findType(TypeRef.ById(it.typeId)).key.value,
                optional = it.optional,
                tags = it.tags.map { it.value.toString() }
            )
        }
    }
}