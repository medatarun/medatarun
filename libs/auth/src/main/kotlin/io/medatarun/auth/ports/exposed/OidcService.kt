package io.medatarun.auth.ports.exposed

import io.medatarun.auth.domain.oidc.OidcAuthorizeCode
import io.medatarun.auth.domain.oidc.OidcAuthorizeCtx
import io.medatarun.auth.domain.oidc.OidcAuthorizeRequest
import io.medatarun.auth.domain.oidc.OidcTokenRequest
import io.medatarun.auth.internal.oidc.OidcAuthorizeResult
import kotlinx.serialization.json.JsonObject
import java.net.URI

interface OidcService {
    /**
     * Returns Json object that matches RFC-7517 JSON Web Key (JWK)
     */
    fun oidcJwks(): JsonObject

    /**
     * URL relative to public base URL where the JWKS is located
     */
    fun oidcJwksUri(): String

    /**
     * Returns our issuer as configured.
     *
     * This is used in many parts to know "it is use who issued the key"
     *
     * Changes some behaviors in other parts of the system to distinguish
     * external sources (external IdP for example) from an internal source (our
     * internal IdP)
     */
    fun oidcIssuer(): String

    /**
     * Provides a JWT verifier resolver so the HTTP layer can stay minimal and delegate
     * verification rules to the auth domain.
     */
    fun jwtVerifierResolver(): JwtVerifierResolver

    /**
     * URI relative to public base URL to get the well-known openId configuration
     */
    fun oidcWellKnownOpenIdConfigurationUri(): String

    /**
     * Contents of well-known openid configuration in Json.
     * Must respect the standard
     */
    fun oidcWellKnownOpenIdConfiguration(publicBaseUrl: URI): JsonObject

    /**
     * Returns URL where authorize is done. Implementation shall return a URL where
     * users can put their login and password.
     */
    fun oidcAuthorizeUri(): String

    /**
     * Creates an Authorize code based on request.
     * If there are errors on the request, returns an error and data that will allow use to build an error URL.
     * If it is ok, creates a context identified by "authCtxCode" and persist it in storage. Also includes a URL to our login page.
     * This context will be re-read after a successful login to redirect the user to its client application.
     */
    fun oidcAuthorize(req: OidcAuthorizeRequest, publicBaseUrl: URI): OidcAuthorizeResult

    /**
     * Finds an authorized context by its identifier (identifier is an [authCtxCode])
     */
    fun oidcAuthorizeFindAuthCtx(authCtxCode: String): OidcAuthorizeCtx?

    /**
     * Called after a successful login. Then, the authCtxCode and the subject (= user login)
     * are used to transform the auth context into an [OidcAuthorizeCode].
     *
     * This code with all the context will be stored along with the subject.
     *
     * Then we return a redirect URI, based on the initial redirect uri, that contains the code
     *
     * UI will redirect user to its client application using this URI. The user's OIDC client
     * will call /token and give /token this code again, so OIDC the process can continue.
     */
    fun oidcAuthorizeCreateCode(authCtxCode: String, subject: String): String

    fun oidcTokenUri(): String
    fun oidcToken(oidcTokenReq: OidcTokenRequest): OIDCTokenResponseOrError

    fun oidcAuthorizeErrorLocation(resp: OidcAuthorizeResult.RedirectError): String
    fun oidcAuthority(publicBaseUrl: URI): URI
    fun oidcClientId(): String


}
