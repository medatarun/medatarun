package io.medatarun.auth.ports.needs

import io.medatarun.auth.domain.AuthCode
import io.medatarun.auth.domain.AuthCtx
import java.time.Instant

interface AuthorizeStorage {

    // ------------------------------------------------------------------------
    // AuthCtx management
    //
    // data from /oidc/authorize -> /ui/auth/login
    //
    // Stores navigation context from /oidc/authorize to login page,
    // and login page loops until success
    // ------------------------------------------------------------------------

    fun saveAuthCtx(authCtxId: AuthCtx)
    fun findAuthCtx(authCtxId: String): AuthCtx
    fun deleteAuthCtx(authorizeCtxCode: String)

    // ------------------------------------------------------------------------
    // AuthCode management
    //
    // login success -> redirectUri?code=xxx -> /oidc/token?code=xxx
    //
    // Stores context associated with this code
    // ------------------------------------------------------------------------

    fun saveAuthCode(authCode: AuthCode)
    fun findAuthCode(authCode: String): AuthCode?
    fun deleteAuthCode(authCode: String)



    fun purgeExpired(now: Instant)
}
