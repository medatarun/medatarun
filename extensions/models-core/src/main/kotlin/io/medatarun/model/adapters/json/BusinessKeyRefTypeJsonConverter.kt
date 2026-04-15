package io.medatarun.model.adapters.json

import io.medatarun.model.domain.BusinessKeyId
import io.medatarun.model.domain.BusinessKeyKey
import io.medatarun.model.domain.BusinessKeyRef
import io.medatarun.type.commons.ref.RefTypeJsonConverters
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement

class BusinessKeyRefTypeJsonConverter : TypeJsonConverter<BusinessKeyRef> {
    override fun deserialize(json: JsonElement): BusinessKeyRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id -> BusinessKeyRef.ById(BusinessKeyId.fromString(id)) },
            whenKey = { key -> BusinessKeyRef.ByKey(BusinessKeyKey(key)) }
        )
    }

}
