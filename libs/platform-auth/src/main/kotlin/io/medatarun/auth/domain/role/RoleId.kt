package io.medatarun.auth.domain.role

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.UUID

data class RoleId(override val value: UUID): Id<RoleId> {
    companion object {
        fun generate(): RoleId {
            return Id.generate(::RoleId)
        }
    }
}
