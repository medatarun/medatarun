package io.medatarun.model.domain

import io.medatarun.model.internal.KeyValidation

@JvmInline value class ModelKey(val value: String) {
    fun validated(): ModelKey {
        KeyValidation.validate(this@ModelKey.value)
        return this
    }
}