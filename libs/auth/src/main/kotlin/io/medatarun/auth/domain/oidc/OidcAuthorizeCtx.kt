package io.medatarun.auth.domain.oidc

import java.time.Instant

/**
 * Represent the session created by /oidc/authorize
 * and uniquely identified by authorizeCtxCode
 *
 * data from /oidc/authorize -> /ui/auth/login
 *
 * Stores navigation context from /oidc/authorize to login page,
 * and login page loops until success
 */
data class OidcAuthorizeCtx(
    /**
     * Unique identifier of this context
     */
    val authCtxCode: String,
    /**
     * Client id making the OIDC request.
     */
    val clientId: String,
    /**
     * Chosen redirect URI from the client
     */
    val redirectUri: String,
    /**
     * Scope
     */
    val scope: String,
    /**
     * State send by the client that we must send him back when login will be successful
     */
    val state: String?,
    /**
     * Code challenge sent by client
     */
    val codeChallenge: String,
    /**
     * Code challenge method sent by client (always the same for the moment)
     */
    val codeChallengeMethod: String,
    /**
     * Nonce sent by client
     */
    val nonce: String?,
    /**
     * Instant this context is created
     */
    val createdAt: Instant,
    /**
     * When this context will expire
     */
    val expiresAt: Instant
)