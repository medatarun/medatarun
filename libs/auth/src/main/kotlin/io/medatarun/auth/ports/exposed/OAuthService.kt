package io.medatarun.auth.ports.exposed

import io.medatarun.auth.domain.Actor
import io.medatarun.auth.domain.PasswordClear
import io.medatarun.auth.domain.User
import io.medatarun.auth.domain.Username

/**
 * Issue an OAuth token response containing an OAuth token (also known
 * as access_token in OIDC contexts). Note that the token returned here
 * for OAuth must be the same as the token returned by OIDC's access_token.
 *
 * That's why OidcService should depend on this service to not
 * reinvent the wheel and keep this strategy clear.
 *
 * Also note that claims that represent a user in OAuth tokens (access_token)
 * must be the same as in id_token (for OIDC) so we mutualize efforts.
 *
 * It is not required at all by protocol but we choose to do so for simplicity.
 */
interface OAuthService {

    /**
     * Issues a [OAuthTokenResponse] containing an OAuth token (access_token)
     * given a username (login) and password.
     */
    fun oauthLogin(username: Username, password: PasswordClear  ): OAuthTokenResponse

    /**
     * Creates an OAuth access token, for any [User] of the embedded user storage.
     *
     * Same as [oauthLogin] but without the authentication part, just by looking
     * at [User] a user.
     */
    fun createOAuthAccessTokenForUser(user: User): OAuthTokenResponse

    /**
     * Creates an OAuth access token, for an [Actor] in the known actor list.
     *
     * This is mainly used in OIDC processes when we need to issue a token for
     * an actor (from any IdP source).
     */
    fun createOAuthAccessTokenForActor(actor: Actor): OAuthTokenResponse
}