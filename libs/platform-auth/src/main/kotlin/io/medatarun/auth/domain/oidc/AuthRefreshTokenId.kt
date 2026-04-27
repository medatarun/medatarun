package io.medatarun.auth.domain.oidc

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.UUID

@JvmInline
value class AuthRefreshTokenId(override val value: UUID) : Id<AuthRefreshTokenId> {
    companion object {
        fun generate(): AuthRefreshTokenId {
            return AuthRefreshTokenId(UuidUtils.generateV7())
        }
    }
}
