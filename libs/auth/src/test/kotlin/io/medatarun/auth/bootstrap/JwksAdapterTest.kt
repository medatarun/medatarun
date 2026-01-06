package io.medatarun.auth.bootstrap

import io.medatarun.auth.bootstrap.internal.JwksAdapter
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JwksAdapterTest {

    @Test
    fun `toJwks should expose kid n and e from a generated RSA key`() {
        // Arrange
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        val kp = kpg.generateKeyPair()
        val pub = kp.public as RSAPublicKey
        val kid = "kid-123"

        val expectedN = b64url(unsigned(pub.modulus))
        val expectedE = b64url(unsigned(pub.publicExponent))

        // Act
        val jwks = JwksAdapter.toJwks(pub, kid)

        // Assert
        assertNotNull(jwks)
        assertEquals(1, jwks.keys.size)
        val jwk = jwks.keys.single()
        assertEquals("RSA", jwk.kty)
        assertEquals("sig", jwk.use)
        assertEquals("RS256", jwk.alg)
        assertEquals(kid, jwk.kid)
        assertEquals(expectedN, jwk.n, "modulus (n) must be base64url-encoded unsigned bytes")
        assertEquals(expectedE, jwk.e, "exponent (e) must be base64url-encoded unsigned bytes")
    }

    @Test
    fun `toJwks should trim leading 0x00 from BigInteger toByteArray when encoding`() {
        // Create a modulus whose most significant bit is 1, which makes BigInteger.toByteArray() produce a leading 0x00
        val modulusBytes = ByteArray(64) { 0xAA.toByte() } // 512-bit pattern 0xAA...AA (top bit set)
        modulusBytes[0] = 0xFF.toByte() // ensure MSB is 1

        val modulus = BigInteger(1, modulusBytes) // positive big integer
        val exponent = BigInteger.valueOf(65537L)

        val spec = RSAPublicKeySpec(modulus, exponent)
        val kf = KeyFactory.getInstance("RSA")
        val pub = kf.generatePublic(spec) as RSAPublicKey

        val kid = "kid-leading-zero"

        // Expected values: adapter should strip the leading 0x00 that BigInteger.toByteArray() would add
        val expectedN = b64url(modulusBytes) // unsigned representation equals original bytes
        val expectedE = b64url(unsigned(exponent))

        // Sanity check: BigInteger.toByteArray() actually has a leading zero for this modulus
        val bigIntBytes = modulus.toByteArray()
        require(bigIntBytes[0].toInt() == 0) { "Test precondition failed: modulus.toByteArray() should start with 0x00" }

        // Act
        val jwks = JwksAdapter.toJwks(pub, kid)

        // Assert
        val jwk = jwks.keys.single()
        assertEquals(expectedN, jwk.n)
        assertEquals(expectedE, jwk.e)
    }

    // --- helpers (replicate adapter encoding rules) ---
    private fun b64url(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    private fun unsigned(bi: BigInteger): ByteArray {
        val arr = bi.toByteArray()
        return if (arr.isNotEmpty() && arr[0].toInt() == 0) arr.copyOfRange(1, arr.size) else arr
    }
}