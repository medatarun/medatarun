package io.medatarun.auth.bootstrap

interface AuthEmbeddedJwtTokenIssuer {
    /**
     * Creates a Jwt token
     *
     * @param sub subject (principal) = actor identifier = (user or service) identifier
     * @param claims list of claims to add to the token
     *
     */
    fun issueToken(sub: String, claims: Map<String, Any?>): String
}
