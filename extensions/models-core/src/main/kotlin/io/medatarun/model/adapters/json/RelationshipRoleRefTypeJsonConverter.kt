package io.medatarun.model.adapters.json

import io.medatarun.model.domain.*
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement
import java.util.*

class RelationshipRoleRefTypeJsonConverter : TypeJsonConverter<RelationshipRoleRef> {
    override fun deserialize(json: JsonElement): RelationshipRoleRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                RelationshipRoleRef.ById(RelationshipRoleId(UUID.fromString(id)))
            },
            whenKey = { keyParts ->
                RelationshipRoleRef.ByKey(
                    model = ModelKey(keyParts.required("model")),
                    relationship = RelationshipKey(keyParts.required("relationship")),
                    role = RelationshipRoleKey(keyParts.required("role")),
                )
            }
        )
    }

}
