package io.medatarun.auth.internal

sealed interface AuthorizeResult {
    data class Valid(
        val authCtxCode: String
    ) : AuthorizeResult

    data class RedirectError(
        val redirectUri: String,
        val error: String,
        val state: String?
    ) : AuthorizeResult

    data class FatalError(
        val reason: String
    ) : AuthorizeResult
}