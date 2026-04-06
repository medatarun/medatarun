package io.medatarun.auth.domain.user

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class UserId(override val value: UUID): Id<UserId> {
    companion object {
        fun generate(): UserId {
            return UserId(UuidUtils.generateV7())
        }
    }
}