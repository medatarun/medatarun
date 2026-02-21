package io.medatarun.model.domain

import io.medatarun.type.commons.key.KeyValidation


@JvmInline
value class AttributeKey(val value: String) {
    fun validated(): AttributeKey {
        KeyValidation.validate(value)
        return this
    }
}