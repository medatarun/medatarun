package io.medatarun.auth.domain.user

import io.medatarun.lang.uuid.UuidUtils
import java.util.*

@JvmInline
value class UserId(val value: UUID) {
    companion object {
        fun generate(): UserId {
            return UserId(UuidUtils.generateV7())
        }
        fun fromString(value: String): UserId {
            return UserId(UuidUtils.fromString(value))
        }
    }
}