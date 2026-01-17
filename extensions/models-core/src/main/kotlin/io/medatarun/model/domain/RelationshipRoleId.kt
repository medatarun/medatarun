package io.medatarun.model.domain

import io.medatarun.model.internal.KeyValidation

@JvmInline
value class RelationshipRoleId(val value: String) {
    fun validated(): RelationshipRoleId {
        KeyValidation.validate(value)
        return this
    }
}
