package io.medatarun.model.adapters.json

import io.medatarun.model.domain.ModelAuthority
import io.medatarun.type.commons.ref.RefTypeJsonConverters
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement

class ModelAuthorityTypeJsonConverter : TypeJsonConverter<ModelAuthority> {
    override fun deserialize(json: JsonElement): ModelAuthority {
        return RefTypeJsonConverters.expectingString(json) {
            ModelAuthority.valueOfCode(it)
        }
    }
}
