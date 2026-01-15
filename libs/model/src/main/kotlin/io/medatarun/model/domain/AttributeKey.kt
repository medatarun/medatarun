package io.medatarun.model.domain

import io.medatarun.model.internal.KeyValidation

@JvmInline
value class AttributeKey(val value: String) {
    fun validated(): AttributeKey {
        KeyValidation.validate(value)
        return this
    }
}