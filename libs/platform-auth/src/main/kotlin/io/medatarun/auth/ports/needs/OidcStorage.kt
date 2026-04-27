package io.medatarun.auth.ports.needs

import io.medatarun.auth.domain.oidc.OidcAuthorizeCode
import io.medatarun.auth.domain.oidc.OidcAuthorizeCtx
import io.medatarun.auth.domain.oidc.AuthRefreshToken
import io.medatarun.auth.domain.oidc.AuthRefreshTokenId
import java.time.Instant

interface OidcStorage {

    // ------------------------------------------------------------------------
    // AuthCtx management
    //
    // data from /auth/authorize -> /ui/auth/login
    //
    // Stores navigation context from /auth/authorize to login page,
    // and login page loops until success
    // ------------------------------------------------------------------------

    fun saveAuthCtx(oidcAuthorizeCtx: OidcAuthorizeCtx)
    fun findAuthCtx(authCtxId: String): OidcAuthorizeCtx
    fun deleteAuthCtx(authorizeCtxCode: String)

    // ------------------------------------------------------------------------
    // AuthCode management
    //
    // login success -> redirectUri?code=xxx -> /auth/token?code=xxx
    //
    // Stores context associated with this code
    // ------------------------------------------------------------------------

    fun saveAuthCode(oidcAuthorizeCode: OidcAuthorizeCode)
    fun findAuthCode(authCode: String): OidcAuthorizeCode?
    fun deleteAuthCode(authCode: String)

    // ------------------------------------------------------------------------
    // RefreshToken management
    //
    // Stores refresh token metadata. The token itself is not stored: only a
    // hash is persisted so storage content cannot be reused as credentials.
    // ------------------------------------------------------------------------

    fun saveRefreshToken(refreshToken: AuthRefreshToken)
    fun findRefreshTokenByTokenHash(tokenHash: String): AuthRefreshToken?
    fun revokeRefreshToken(id: AuthRefreshTokenId, revokedAt: Instant, replacedById: AuthRefreshTokenId)

    fun purgeExpired(now: Instant)
}
