package io.medatarun.auth.ports.exposed

import io.medatarun.auth.domain.User

interface OAuthService {
    fun oidcLogin(username: String, password: String): OAuthTokenResponse


    /**
     * Creates an OAuth access token, for any user
     */
    fun createOAuthAccessTokenForUser(user: User): OAuthTokenResponse

    /**
     * Creates a Jwt token
     *
     * @param sub subject (principal) = actor identifier = (user or service) identifier
     * @param claims list of claims to add to the token
     *
     */
    fun issueAccessToken(sub: String, claims: Map<String, Any?>): String
}