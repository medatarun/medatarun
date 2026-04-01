package io.medatarun.auth.internal.oidc

import kotlinx.serialization.json.JsonObject
import java.net.URI
import java.time.Instant

data class AuthClient(

    /**
     * Determined by us
     *
     * Unique client id determined at registration time.
     */
    val clientId: String,
    /**
     * How did the client have been registered?
     */
    val origin: OidcClientOrigin,
    /**
     * Original registration payload for DCRP clients
     */
    val originalRegistrationPayload: JsonObject?,
    /**
     * When the client was created in storage.
     */
    val createdAt: Instant,
    /**
     * Last usage timestamp tracked by storage.
     */
    val lastUsedAt: Instant,

    // -------------------------------------------------------------------------
    // same fields as Dynamic Client Registration Protocol
    // -------------------------------------------------------------------------

    /**
     * Array of redirection URI strings for use in redirect-based flows such
     * as the authorization code and implicit flows. As required by Section 2
     * of OAuth 2.0 [RFC 6749], clients using flows with redirection MUST
     * register their redirection URI values. Authorization servers that
     * support dynamic registration for redirect-based flows MUST implement
     * support for this metadata value.
     **/
    val redirectUris: List<URI>,
    /**
     * Array of OAuth 2.0 grant type strings that the client can use at the
     * token endpoint.
     *
     * Possibles values described in the docs.
     *
     * They include "authorization_code", "implicit", "password",
     * "client_credentials", "refresh_tokens"
     * and special "urn:xxx"
     */
    val grantTypes: List<String>,
    /**
     * Array of the OAuth 2.0 response type strings that the client can
     * use at the authorization endpoint.
     *
     * Possible values include "code" and "token" and are described in RFC
     */
    val responseTypes: List<String>,
    /**
     * String indicator of the requested authentication method for the token
     * endpoint.
     *
     * Possible values described in the spec.
     *
     * It includes "none" (The client is a public client),
     * "client_secret_post", "client_secret_basic"
     */
    val tokenEndpointAuthMethod: String,
    /**
     * Human-readable string name of the client to be presented to the
     * end-user during authorization.  If omitted, the authorization server
     * MAY display the raw "client_id" value to the end-user instead.
     **/
    val clientName: String?,
    /**
     * URL string of a web page providing information about the client.
     * If present, the server SHOULD display this URL to the end-user in
     * a clickable fashion.  It is RECOMMENDED that clients always send
     * this field.  The value of this field MUST point to a valid web
     * page.
     */
    val clientUri: URI?,
    /**
     * URL string that references a logo for the client.  If present,
     * the server SHOULD display this image to the end-user during approval.
     * The value of this field MUST point to a valid image file.
     */
    val logoUri: URI?,
    /**
     * Unused yet, but we want to see what values we receive
     */
    val contacts: List<String>,
    /**
     * Unused yet, but we want to see what values we receive
     */
    val softwareId: String?,

    /**
     * Unused yet, but we want to see what values we receive
     */
    val softwareVersion: String?,
    /**
     *
     * URL string that points to a human-readable terms of service
     * document for the client that describes a contractual relationship
     * between the end-user and the client that the end-user accepts when
     * authorizing the client
     *
     * Unused yet, but we want to see what values we receive
     */
    val tosURI: URI?,
    /**
     * URL string that points to a human-readable privacy policy document
     * that describes how the deployment organization collects, uses, retains,
     * and discloses personal data
     */
    val policyURI: URI?,

    )
