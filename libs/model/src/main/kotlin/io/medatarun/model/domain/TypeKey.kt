package io.medatarun.model.domain

import io.medatarun.model.internal.KeyValidation

@JvmInline
value class TypeKey(val value : String ) {
    fun validated(): TypeKey {
        KeyValidation.validate(value)
        return this
    }
}