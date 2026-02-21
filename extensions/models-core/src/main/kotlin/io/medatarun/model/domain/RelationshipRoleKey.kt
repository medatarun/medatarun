package io.medatarun.model.domain

import io.medatarun.type.commons.key.KeyValidation


@JvmInline
value class RelationshipRoleKey(val value: String) {
    fun validated(): RelationshipRoleKey {
        KeyValidation.validate(value)
        return this
    }
}
