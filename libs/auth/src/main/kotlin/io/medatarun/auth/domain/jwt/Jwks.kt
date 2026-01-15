package io.medatarun.auth.domain.jwt

import kotlinx.serialization.Serializable

/**
 * JSON representation of Jwks (Json Web Key Set) meant to be returned
 * when an OIDC client wants to know what public keys we manage in embedded auth server.
 *
 * Each entry is a public key with: key type (RSA), algorithm (RS256), identifier (kid) and mathematical parameters of the key.
 *
 * Not private keys inside, only public ones.
 */
@Serializable
data class Jwks(val keys: List<Jwk>)
