package io.medatarun.model.adapters.json

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.ModelVersion
import io.medatarun.type.commons.ref.RefTypeJsonConverters
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonConverterBadFormatException
import kotlinx.serialization.json.JsonElement

class ModelVersionTypeJsonConverter : TypeJsonConverter<ModelVersion> {
    override fun deserialize(json: JsonElement): ModelVersion {
        return RefTypeJsonConverters.expectingString(json) { value ->
            try {
                ModelVersion(value)
            } catch (e: MedatarunException) {
                throw TypeJsonConverterBadFormatException(e.message ?: "Invalid model version")
            }
        }
    }
}
