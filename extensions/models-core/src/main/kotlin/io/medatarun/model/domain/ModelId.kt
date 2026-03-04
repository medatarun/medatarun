package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class ModelId(override val value: UUID) : Id<ModelId> {
    companion object {
        fun generate(): ModelId {
            return ModelId(UuidUtils.generateV7())
        }

        fun fromString(value: String): ModelId {
            return ModelId(UuidUtils.fromString(value))
        }
    }
}
