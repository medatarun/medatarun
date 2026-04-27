package io.medatarun.auth.domain.oidc

import java.time.Instant

/**
 * Stored state for an OAuth refresh token issued by the Medatarun OIDC token
 * endpoint.
 *
 * The raw refresh token is returned to the client only once and is never stored.
 * Storage keeps [tokenHash] so a token presented later can be verified without
 * keeping a reusable credential server-side.
 *
 * A successful refresh creates a new row and marks the previous one with
 * [revokedAt] and [replacedById]. This makes the previous refresh token
 * unusable while keeping a direct link to the replacement row.
 */
data class AuthRefreshToken(
    /**
     * Stable storage identifier for this refresh token record.
     *
     * This is the row identity used by storage. It is not sent to the client
     * and is not used as a credential.
     */
    val id: AuthRefreshTokenId,
    /**
     * Hash of the refresh token value sent to the client.
     *
     * The client receives the raw refresh token, but storage only keeps this
     * hash. When the client later presents the raw token, the server hashes it
     * again and looks up the matching record. This avoids keeping reusable
     * refresh token credentials in storage (security measure).
     */
    val tokenHash: String,
    /**
     * OIDC client that received this refresh token.
     *
     * A refresh request is accepted only when its `client_id` matches this
     * value, so a token issued to one client cannot be used by another client.
     */
    val clientId: String,
    /**
     * Subject for which new access and ID tokens can be issued.
     *
     * During refresh, the service uses this subject with the internal issuer to
     * reload the current actor and build fresh claims.
     *
     * We store the OIDC subject, not the Medatarun actor id. This record belongs
     * to the OAuth/OIDC token flow, and OAuth/OIDC identifies a user with
     * `issuer + subject`. The actor id is a Medatarun internal storage id. Using
     * it here would couple refresh tokens to the actor table instead of the token
     * identity that appears in JWT `sub` claims.
     */
    val subject: String,
    /**
     * Scopes granted during the initial authorization code flow.
     *
     * This records what the session was allowed to request when the refresh
     * token was issued. It is kept with the token so refresh processing does
     * not rely on a new authorization request.
     */
    val scope: String,
    /**
     * Time when the user authenticated with login and password.
     *
     * Refreshed ID tokens reuse this value because refresh renews tokens but
     * does not authenticate the user again.
     */
    val authTime: Instant,
    /**
     * Time after which this refresh token can no longer be used.
     *
     * Refresh requests after this instant are rejected with `invalid_grant`.
     */
    val expiresAt: Instant,
    /**
     * Time when this refresh token was made unusable.
     *
     * This is set when the token is replaced by a successful refresh. Any later
     * use of this token is rejected with `invalid_grant`.
     */
    val revokedAt: Instant?,
    /**
     * Storage id of the refresh token row that replaced this one.
     *
     * Hashes are for matching credentials presented by clients. Row ids are for
     * linking stored records together. This field therefore points to [id] of
     * the replacement row, not to its token hash.
     */
    val replacedById: AuthRefreshTokenId?,
    /**
     * Nonce from the original authorization request, reused in refreshed ID tokens.
     *
     * If the initial OIDC authorization request provided a nonce, refreshed ID
     * tokens keep carrying it consistently with the original session.
     */
    val nonce: String?
)
