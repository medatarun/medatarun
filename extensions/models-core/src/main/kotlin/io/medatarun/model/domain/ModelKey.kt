package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.key.Key

@JvmInline
value class ModelKey(override val value: String) : Key<ModelKey> {
    companion object {
        fun generateRandom(): ModelKey {
            return ModelKey(UuidUtils.generateV4String())
        }
    }
}