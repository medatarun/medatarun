package io.medatarun.tags.core.domain

import io.medatarun.type.commons.key.KeyValidation

@JvmInline
value class TagManagedKey(val value: String) {
    fun validated(): TagManagedKey {
        KeyValidation.validate(value)
        return this
    }
}
