package io.medatarun.model.domain

import io.medatarun.model.internal.KeyValidation

@JvmInline
value class RelationshipKey(val value: String) {
    fun validated(): RelationshipKey {
        KeyValidation.validate(value)
        return this
    }
}