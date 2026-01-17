package io.medatarun.model.domain

import io.medatarun.model.internal.KeyValidation

@JvmInline
value class EntityKey(val value: String) {
    fun validated(): EntityKey {
        KeyValidation.validate(value)
        return this
    }
}