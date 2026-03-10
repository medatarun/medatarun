package io.medatarun.model.adapters.json

import io.medatarun.model.domain.diff.ModelDiffScope
import io.medatarun.type.commons.ref.RefTypeJsonConverters
import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement

class ModelDiffScopeTypeJsonConverter : TypeJsonConverter<ModelDiffScope> {
    override fun deserialize(json: JsonElement): ModelDiffScope {
        return RefTypeJsonConverters.expectingString(json) {
            ModelDiffScope.valueOfCodeOptional(it)
                ?: throw TypeJsonConverterBadFormatException("Unknown model diff scope code in JSON: $it")
        }
    }
}
