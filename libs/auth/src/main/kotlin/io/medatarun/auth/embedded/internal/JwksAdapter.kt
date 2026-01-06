package io.medatarun.auth.embedded.internal

import io.medatarun.auth.embedded.Jwk
import io.medatarun.auth.embedded.Jwks
import java.math.BigInteger
import java.security.interfaces.RSAPublicKey
import java.util.*

object JwksAdapter {

    fun toJwks(publicKey: RSAPublicKey, kid: String): Jwks {
        val n = b64url(publicKey.modulus.toUnsignedBytes())
        val e = b64url(publicKey.publicExponent.toUnsignedBytes())
        return Jwks(keys = listOf(Jwk(kid = kid, n = n, e = e)))
    }

    private fun b64url(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    private fun BigInteger.toUnsignedBytes(): ByteArray {
        val arr = this.toByteArray()
        return if (arr.isNotEmpty() && arr[0].toInt() == 0) arr.copyOfRange(1, arr.size) else arr
    }

}