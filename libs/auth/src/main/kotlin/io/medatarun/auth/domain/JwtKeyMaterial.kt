package io.medatarun.auth.domain

import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

data class JwtKeyMaterial(
    val privateKey: RSAPrivateKey,
    val publicKey: RSAPublicKey,
    val kid: String
)