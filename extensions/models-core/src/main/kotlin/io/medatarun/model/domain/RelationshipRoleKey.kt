package io.medatarun.model.domain

import io.medatarun.model.internal.KeyValidation

@JvmInline
value class RelationshipRoleKey(val value: String) {
    fun validated(): RelationshipRoleKey {
        KeyValidation.validate(value)
        return this
    }
}
