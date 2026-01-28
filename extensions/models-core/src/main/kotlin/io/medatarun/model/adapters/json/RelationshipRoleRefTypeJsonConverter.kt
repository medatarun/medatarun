package io.medatarun.model.adapters.json

import io.medatarun.model.domain.RelationshipRoleId
import io.medatarun.model.domain.RelationshipRoleKey
import io.medatarun.model.domain.RelationshipRoleRef
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement

class RelationshipRoleRefTypeJsonConverter : TypeJsonConverter<RelationshipRoleRef> {
    override fun deserialize(json: JsonElement): RelationshipRoleRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                RelationshipRoleRef.ById(RelationshipRoleId.fromString(id))
            },
            whenKey = { key -> RelationshipRoleRef.ByKey(RelationshipRoleKey(key)) }
        )
    }

}
