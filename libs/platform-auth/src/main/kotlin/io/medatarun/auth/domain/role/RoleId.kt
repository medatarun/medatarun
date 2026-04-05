package io.medatarun.auth.domain.role

import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class RoleId(override val value: UUID): Id<RoleId> {
    companion object {
        fun generate(): RoleId {
            return Id.generate(::RoleId)
        }
    }
}
