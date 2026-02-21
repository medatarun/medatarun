package io.medatarun.tags.core.domain

import io.medatarun.type.commons.key.KeyValidation

@JvmInline
value class TagGroupKey(val value: String) {
    fun validated(): TagGroupKey {
        KeyValidation.validate(value)
        return this
    }

    fun asString(): String {
        return value
    }
}
