package io.medatarun.auth.ports.needs

import io.medatarun.auth.domain.oidc.OidcAuthorizeCode
import io.medatarun.auth.domain.oidc.OidcAuthorizeCtx
import java.time.Instant

interface OidcStorage {

    // ------------------------------------------------------------------------
    // AuthCtx management
    //
    // data from /oidc/authorize -> /ui/auth/login
    //
    // Stores navigation context from /oidc/authorize to login page,
    // and login page loops until success
    // ------------------------------------------------------------------------

    fun saveAuthCtx(oidcAuthorizeCtxId: OidcAuthorizeCtx)
    fun findAuthCtx(authCtxId: String): OidcAuthorizeCtx
    fun deleteAuthCtx(authorizeCtxCode: String)

    // ------------------------------------------------------------------------
    // AuthCode management
    //
    // login success -> redirectUri?code=xxx -> /oidc/token?code=xxx
    //
    // Stores context associated with this code
    // ------------------------------------------------------------------------

    fun saveAuthCode(oidcAuthorizeCode: OidcAuthorizeCode)
    fun findAuthCode(authCode: String): OidcAuthorizeCode?
    fun deleteAuthCode(authCode: String)



    fun purgeExpired(now: Instant)
}
