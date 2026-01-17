package io.medatarun.auth.internal.oidc

import java.net.URI

data class OidcClient(
    val clientId: String,
    val redirectUris: List<URI>
)