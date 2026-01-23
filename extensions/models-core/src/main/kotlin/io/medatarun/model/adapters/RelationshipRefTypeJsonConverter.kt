package io.medatarun.model.adapters

import io.medatarun.model.adapters.RefTypeJsonConverters.decodeRef
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.RelationshipKey
import io.medatarun.model.domain.RelationshipRef
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement
import java.util.*

class RelationshipRefTypeJsonConverter : TypeJsonConverter<RelationshipRef> {
    override fun deserialize(json: JsonElement): RelationshipRef {
        return decodeRef(
            json,
            whenId = { id ->
                RelationshipRef.ById(RelationshipId(UUID.fromString(id)))
            },
            whenKey = { keyParts ->
                RelationshipRef.ByKey(
                    model = ModelKey(keyParts.required("model")),
                    relationship = RelationshipKey(keyParts.required("relationship")),
                )
            }
        )
    }

}
