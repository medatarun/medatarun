package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class ModelEventId(override val value: UUID) : Id<ModelEventId> {
    companion object {
        fun generate(): ModelEventId {
            return ModelEventId(UuidUtils.generateV7())
        }

        fun fromString(value: String): ModelEventId {
            return ModelEventId(UuidUtils.fromString(value))
        }
    }
}
