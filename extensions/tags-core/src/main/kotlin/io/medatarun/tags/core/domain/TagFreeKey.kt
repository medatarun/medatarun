package io.medatarun.tags.core.domain

import io.medatarun.type.commons.key.KeyValidation

@JvmInline
value class TagFreeKey(val value: String) {
    fun validated(): TagFreeKey {
        KeyValidation.validate(value)
        return this
    }
}
