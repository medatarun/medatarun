package io.medatarun.auth.internal.oidc

data class OidcClient(
    val clientId: String,
    val redirectUris: List<String>
) {

}