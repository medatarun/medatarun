package io.medatarun.ext.modeljson.internal.base

import io.medatarun.ext.modeljson.internal.serializers.LocalizedTextMultiLangCompat
import io.medatarun.model.domain.*

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
                name = LocalizedTextMultiLangCompat.ofOptional(it.name),
                description = LocalizedTextMultiLangCompat.ofOptional(it.description),
                type = model.findType(TypeRef.ById(it.typeId)).key.value,
                optional = it.optional,
                tags = it.tags.map { it.value.toString() }
            )
        }
    }

    fun toTypeJsonList(model: ModelAggregate): List<ModelTypeJson> {
        return model.types.map { type ->
            ModelTypeJson(
                id = type.id.value.toString(),
                key = type.key.value,
                name = LocalizedTextMultiLangCompat.ofOptional(type.name),
                description = LocalizedTextMultiLangCompat.ofOptional(type.description),
            )
        }
    }

    fun toRelationshipJsonList(model: ModelAggregate): List<RelationshipJson> {
        return model.relationships.map { rel ->
            RelationshipJson(
                id = rel.id.value.toString(),
                key = rel.key.value,
                name = LocalizedTextMultiLangCompat.ofOptional(rel.name),
                description = LocalizedTextMultiLangCompat.ofOptional(rel.description),
                roles = rel.roles.map { role ->
                    RelationshipRoleJson(
                        id = role.id.value.toString(),
                        key = role.key.value,
                        entityId = model.findEntity(role.entityId).key.value,
                        name = LocalizedTextMultiLangCompat.ofOptional(role.name),
                        cardinality = role.cardinality.code
                    )
                },
                attributes = toAttributeJsonList(model, model.attributes.filter { it.ownedBy(rel.id) }),
                tags = rel.tags.map { it.value.toString() }

            )
        }
    }

    fun toTagList(model: ModelAggregate): List<String> {
        return model.tags.map { it.value.toString() }
    }
}