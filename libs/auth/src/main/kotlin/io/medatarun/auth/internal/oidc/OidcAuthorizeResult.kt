package io.medatarun.auth.internal.oidc

sealed interface OidcAuthorizeResult {
    data class Valid(
        val authCtxCode: String
    ) : OidcAuthorizeResult

    data class RedirectError(
        val redirectUri: String,
        val error: String,
        val state: String?
    ) : OidcAuthorizeResult

    data class FatalError(
        val reason: String
    ) : OidcAuthorizeResult
}