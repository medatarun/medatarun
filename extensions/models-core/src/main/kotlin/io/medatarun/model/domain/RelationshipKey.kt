package io.medatarun.model.domain

import io.medatarun.type.commons.key.KeyValidation


@JvmInline
value class RelationshipKey(val value: String) {
    fun validated(): RelationshipKey {
        KeyValidation.validate(value)
        return this
    }
}