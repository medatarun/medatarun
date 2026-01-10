package io.medatarun.auth.internal

data class OidcClient(
    val clientId: String,
    val redirectUris: List<String>
) {

}