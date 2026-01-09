package io.medatarun.auth.internal

import io.medatarun.auth.domain.AuthCode
import io.medatarun.auth.domain.AuthCtx
import io.medatarun.auth.domain.OIDCAuthorizeRequest
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.auth.ports.needs.AuthorizeStorage
import java.net.URLEncoder
import java.util.*

class AuthEmbeddedOIDCAuthorizeService(
    private val storage: AuthorizeStorage,
    private val clock: AuthClock,
    private val authCtxDurationSeconds: Long
) {

    val clients = listOf<OidcClient>(
        OidcClient(
            "medatarun-ui",
            listOf("http://localhost:8080/authentication-callback"),
        ),
        OidcClient(
            "seij-gestion",
            listOf("http://localhost:4200/authentication-callback"),
        )
    ).associateBy { it.clientId }


    fun authorize(req: OIDCAuthorizeRequest): AuthorizeResult {

        val redirectUri = req.redirectUri
            ?: return AuthorizeResult.FatalError("missing redirect_uri")

        if (req.responseType != "code") {
            return AuthorizeResult.RedirectError(
                redirectUri, "unsupported_response_type", req.state
            )
        }

        val clientId = req.clientId
            ?: return AuthorizeResult.RedirectError(
                redirectUri, "unauthorized_client", req.state
            )

        val client = clients[clientId]
            ?: return AuthorizeResult.RedirectError(
                redirectUri, "unauthorized_client", req.state
            )

        if (!client.redirectUris.contains(redirectUri)) {
            return AuthorizeResult.FatalError("invalid redirect_uri")
        }

        val scope = req.scope
            ?: return AuthorizeResult.RedirectError(
                redirectUri, "invalid_scope", req.state
            )
        if (!scope.split(" ").contains("openid")) {
            return AuthorizeResult.RedirectError(
                redirectUri, "invalid_scope", req.state
            )
        }

        val state = req.state

        val codeChallenge = req.codeChallenge
            ?: return AuthorizeResult.RedirectError(
                redirectUri,
                "invalid_request",
                state
            )

        if (req.codeChallengeMethod != "S256") {
            return AuthorizeResult.RedirectError(
                redirectUri,
                "invalid_request",
                state
            )
        }
        val codeChallengeMethod = "S256"

        val nonce = req.nonce

        val authorizeCtxCode = UUID.randomUUID().toString()

        val createdAt = clock.now()
        val expiresAt = createdAt.plusSeconds(authCtxDurationSeconds)

        storage.saveAuthCtx(
            AuthCtx(
                clientId = clientId,
                redirectUri = redirectUri,
                scope = scope,
                state = state,
                codeChallenge = codeChallenge,
                codeChallengeMethod = codeChallengeMethod,
                authCtxCode = authorizeCtxCode,
                nonce = nonce,
                createdAt = clock.now(),
                expiresAt = clock.now().plusSeconds(60*15),
            )
        )
        return AuthorizeResult.Valid(authCtxCode = authorizeCtxCode)

    }

    fun createRedirectErrorLocation(err: AuthorizeResult.RedirectError): String {
        return buildString {
            append(err.redirectUri)
            append("?error=")
            append(URLEncoder.encode(err.error, Charsets.UTF_8))
            if (err.state != null) {
                append("&state=")
                append(URLEncoder.encode(err.state, Charsets.UTF_8))
            }
        }

    }

    fun authorize(authorizeCtxCode: String, login: String): String {
        val authorizeCtx = storage.findAuthCtx(authorizeCtxCode)

        val code = UUID.randomUUID().toString()

        val now = clock.now()

        val authCode = AuthCode(
            code = code,
            clientId = authorizeCtx.clientId,
            redirectUri = authorizeCtx.redirectUri,
            subject = login,
            scope = authorizeCtx.scope,
            codeChallenge = authorizeCtx.codeChallenge,
            codeChallengeMethod = authorizeCtx.codeChallengeMethod,
            nonce = authorizeCtx.nonce,
            authTime = now,
            expiresAt = now.plusSeconds(120)
        )

        // tu stockes authCode dans une map / DB
        storage.deleteAuthCtx(authorizeCtxCode)
        storage.saveAuthCode(authCode)

        // tu envoies *uniquement*, la string au client
        return buildRedirectUri(authCode.redirectUri,  authorizeCtx.state, code)
    }

    fun buildRedirectUri(redirectUri: String, state: String?, authorizationCode: String): String {
        return buildString {
            append(redirectUri)
            append("?code=")
            append(URLEncoder.encode(authorizationCode, Charsets.UTF_8))
            if (state != null) {
                append("&state=")
                append(URLEncoder.encode(state, Charsets.UTF_8))
            }
        }
    }

    fun findAuthCtx(authCtxCode: String): AuthCtx? {
            return storage.findAuthCtx(authCtxCode)
    }
}