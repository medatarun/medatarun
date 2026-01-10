package io.medatarun.auth.domain

import java.time.Instant

data class OidcAuthorizeCode(
    /**
     * Code that will be sent back to client after /oidc/authorize and that we must get back in /oidc/token
     * Generated on our side at the end of the login process
     */
    val code: String,
    /**
     * Client id that started OIDC process
     */
    val clientId: String,
    /**
     * One of the URLs of the client
     */
    val redirectUri: String,
    /**
     * Subject
     */
    val subject: String,
    /**
     * Scope
     */
    val scope: String,
    /**
     * Code challenge sent by client
     */
    val codeChallenge: String,
    /**
     * Code challenge method sent by client
     */
    val codeChallengeMethod: String,
    /**
     * When user had been successfully authenticated (not when code had been emitted)
     */
    val authTime: Instant,
    /**
     * When this code expires
     */
    val expiresAt: Instant,
    /**
     * Nonce sent by client
     */
    val nonce: String?
)