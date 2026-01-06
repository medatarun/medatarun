package io.medatarun.auth.embedded

import kotlinx.serialization.Serializable

/**
 * Json Web Key (Jwk) representation
 *
 * Meant to be exposed via APIs to publish what keys we store in embedded auth server
 */
@Serializable
data class Jwk(
    /**
     * identifies the key type. For JWKS used with JWT signing, this is typically "RSA".
     */
    val kty: String = "RSA",
    /**
     * indicates the intended use of the key. For JWT signature verification, the value is "sig".
     */
    val use: String = "sig",
    /**
     * specifies the algorithm the key is meant to be used with. Here it is "RS256", meaning RSA with SHA-256.
     */
    val alg: String = "RS256",
    /**
     * is the key identifier. It is a stable identifier used by clients and JWT verifiers to select the correct public key when multiple keys are published.
     */
    val kid: String,
    /**
     * is the RSA modulus, encoded using Base64URL without padding. It represents the main public component of the RSA key.
     */
    val n: String,
    /**
     * is the RSA public exponent, encoded using Base64URL without padding. It is usually a small value such as 65537.
     */
    val e: String
)
