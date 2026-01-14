package io.medatarun.auth.internal.jwk

import com.auth0.jwk.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.medatarun.auth.domain.*
import io.medatarun.auth.domain.oidc.ExternalOidcProviderConfig
import io.medatarun.auth.domain.oidc.ExternalOidcProvidersConfig
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.ECGenParameterSpec
import java.util.*
import kotlin.test.assertFailsWith

class JwtVerifierResolverImplTest {
    private val internalIssuer = "https://issuer.internal.example"
    private val internalAudience = "medatarun-api"
    private val externalIssuer = "https://issuer.external.example"
    private val externalAudience = "external-api"
    private val jwksUri = "https://issuer.external.example/.well-known/jwks.json"

    @Test
    fun `should resolve and verify internal token`() {
        // Purpose: cover internal issuer path without JWKS lookup.
        // How: sign a token with internal issuer and verify using configured public key.
        val keyPair = generateRsaKeyPair()
        val token = createRsaToken(internalIssuer, internalAudience, keyPair, "internal-kid")
        val resolver = JwtVerifierResolverImpl(
            internalIssuer,
            internalAudience,
            keyPair.public as RSAPublicKey,
            JwtExternalProvidersEmpty()
        )

        val verifier = resolver.resolve(token)
        verifier.verify(token)
    }

    @Test
    fun `should resolve and verify external token`() {
        // Purpose: cover external issuer path with JWKS lookup and configured audience.
        // How: sign a token with external issuer and resolve its key via an in-memory JWK provider.
        val keyPair = generateRsaKeyPair()
        val kid = "external-kid"
        val token = createRsaToken(externalIssuer, externalAudience, keyPair, kid)
        val jwkProvider = MapBackedJwkProvider(mapOf(kid to createRsaJwk(kid, keyPair.public as RSAPublicKey)))
        val externalProviders = createExternalProviders(
            allowedAlgs = "RS256",
            jwkProviders = mapOf(externalIssuer to jwkProvider)
        )
        val resolver = JwtVerifierResolverImpl(
            internalIssuer,
            internalAudience,
            generateRsaKeyPair().public as RSAPublicKey,
            externalProviders
        )

        val verifier = resolver.resolve(token)
        verifier.verify(token)
    }

    @Test
    fun `should reject malformed token`() {
        // Purpose: ensure malformed JWTs are rejected early.
        // How: call resolve with a non-JWT string and expect JwtMalformedTokenException.
        val resolver = JwtVerifierResolverImpl(
            internalIssuer,
            internalAudience,
            generateRsaKeyPair().public as RSAPublicKey,
            JwtExternalProvidersEmpty()
        )

        assertFailsWith<JwtMalformedTokenException> {
            resolver.resolve("not-a-jwt")
        }
    }

    @Test
    fun `should reject token without issuer`() {
        // Purpose: require issuer for all tokens.
        // How: sign a token without iss and expect JwtMissingIssuerException.
        val keyPair = generateRsaKeyPair()
        val token = JWT.create()
            .withAudience(internalAudience)
            .withKeyId("internal-kid")
            .sign(
                Algorithm.RSA256(
                    keyPair.public as RSAPublicKey,
                    keyPair.private as RSAPrivateKey
                )
            )
        val resolver = JwtVerifierResolverImpl(
            internalIssuer,
            internalAudience,
            keyPair.public as RSAPublicKey,
            JwtExternalProvidersEmpty()
        )

        assertFailsWith<JwtMissingIssuerException> {
            resolver.resolve(token)
        }
    }

    @Test
    fun `should reject token without alg`() {
        // Purpose: require alg in JWT header to select verification strategy.
        // How: craft a token without alg and expect JwtMissingAlgException.
        val resolver = JwtVerifierResolverImpl(
            internalIssuer,
            internalAudience,
            generateRsaKeyPair().public as RSAPublicKey,
            JwtExternalProvidersEmpty()
        )
        val token = createTokenWithoutAlg(internalIssuer)

        assertFailsWith<JwtMissingAlgException> {
            resolver.resolve(token)
        }
    }

    @Test
    fun `should reject unknown external issuer`() {
        // Purpose: reject tokens from unconfigured external issuers.
        // How: use an issuer not present in config and expect JwtUnknownIssuerException.
        val keyPair = generateRsaKeyPair()
        val token = createRsaToken("https://issuer.unknown.example", externalAudience, keyPair, "kid")
        val externalProviders = createExternalProviders(
            allowedAlgs = "RS256",
            jwkProviders = emptyMap()
        )
        val resolver = JwtVerifierResolverImpl(
            internalIssuer,
            internalAudience,
            generateRsaKeyPair().public as RSAPublicKey,
            externalProviders
        )

        assertFailsWith<JwtUnknownIssuerException> {
            resolver.resolve(token)
        }
    }

    @Test
    fun `should reject external token with unsupported algorithm`() {
        // Purpose: enforce external provider allowed algorithms.
        // How: sign ES256 but allow only RS256 and expect JwtUnsupportedAlgException.
        val keyPair = generateEcKeyPair()
        val token = createEcToken(externalIssuer, externalAudience, keyPair, "kid-ec")
        val externalProviders = createExternalProviders(
            allowedAlgs = "RS256",
            jwkProviders = emptyMap()
        )
        val resolver = JwtVerifierResolverImpl(
            internalIssuer,
            internalAudience,
            generateRsaKeyPair().public as RSAPublicKey,
            externalProviders
        )

        assertFailsWith<JwtUnsupportedAlgException> {
            resolver.resolve(token)
        }
    }

    @Test
    fun `should reject external token without kid`() {
        // Purpose: require kid for external JWKS key resolution.
        // How: sign token without kid and expect JwtMissingKidException.
        val keyPair = generateRsaKeyPair()
        val token = JWT.create()
            .withIssuer(externalIssuer)
            .withAudience(externalAudience)
            .sign(
                Algorithm.RSA256(
                    keyPair.public as RSAPublicKey,
                    keyPair.private as RSAPrivateKey
                )
            )
        val externalProviders = createExternalProviders(
            allowedAlgs = "RS256",
            jwkProviders = emptyMap()
        )
        val resolver = JwtVerifierResolverImpl(
            internalIssuer,
            internalAudience,
            generateRsaKeyPair().public as RSAPublicKey,
            externalProviders
        )

        assertFailsWith<JwtMissingKidException> {
            resolver.resolve(token)
        }
    }

    @Test
    fun `should reject unknown kid from jwk provider`() {
        // Purpose: map missing JWKS key to domain exception.
        // How: configure provider to throw SigningKeyNotFoundException and expect JwtUnknownKidException.
        val keyPair = generateRsaKeyPair()
        val kid = "missing-kid"
        val token = createRsaToken(externalIssuer, externalAudience, keyPair, kid)
        val jwkProvider = MapBackedJwkProvider(
            jwks = emptyMap(),
            exceptions = mapOf(kid to SigningKeyNotFoundException("missing", null))
        )
        val externalProviders = createExternalProviders(
            allowedAlgs = "RS256",
            jwkProviders = mapOf(externalIssuer to jwkProvider)
        )
        val resolver = JwtVerifierResolverImpl(
            internalIssuer,
            internalAudience,
            generateRsaKeyPair().public as RSAPublicKey,
            externalProviders
        )

        assertFailsWith<JwtUnknownKidException> {
            resolver.resolve(token)
        }
    }

    @Test
    fun `should reject jwks fetch failures`() {
        // Purpose: map JWKS provider failures to domain exception.
        // How: configure provider to throw JwkException and expect JwtJwksFetchException.
        val keyPair = generateRsaKeyPair()
        val kid = "network-kid"
        val token = createRsaToken(externalIssuer, externalAudience, keyPair, kid)
        val jwkProvider = MapBackedJwkProvider(
            jwks = emptyMap(),
            exceptions = mapOf(kid to JwkException("down"))
        )
        val externalProviders = createExternalProviders(
            allowedAlgs = "RS256",
            jwkProviders = mapOf(externalIssuer to jwkProvider)
        )
        val resolver = JwtVerifierResolverImpl(
            internalIssuer,
            internalAudience,
            generateRsaKeyPair().public as RSAPublicKey,
            externalProviders
        )

        assertFailsWith<JwtJwksFetchException> {
            resolver.resolve(token)
        }
    }

    @Test
    fun `should reject invalid public key from jwks`() {
        // Purpose: reject invalid JWKS key material.
        // How: configure provider to throw InvalidPublicKeyException and expect JwtUnsupportedKeyTypeException.
        val keyPair = generateRsaKeyPair()
        val kid = "invalid-key"
        val token = createRsaToken(externalIssuer, externalAudience, keyPair, kid)
        val jwkProvider = MapBackedJwkProvider(
            jwks = emptyMap(),
            exceptions = mapOf(kid to InvalidPublicKeyException("bad"))
        )
        val externalProviders = createExternalProviders(
            allowedAlgs = "RS256",
            jwkProviders = mapOf(externalIssuer to jwkProvider)
        )
        val resolver = JwtVerifierResolverImpl(
            internalIssuer,
            internalAudience,
            generateRsaKeyPair().public as RSAPublicKey,
            externalProviders
        )

        assertFailsWith<JwtUnsupportedKeyTypeException> {
            resolver.resolve(token)
        }
    }

    @Test
    fun `should reject mismatched key type for algorithm`() {
        // Purpose: reject JWKS keys that do not match the token algorithm.
        // How: return an EC key for an RS256 token and expect JwtUnsupportedKeyTypeException.
        val rsaKeyPair = generateRsaKeyPair()
        val ecKeyPair = generateEcKeyPair()
        val kid = "ec-key"
        val token = createRsaToken(externalIssuer, externalAudience, rsaKeyPair, kid)
        val ecPublicKey = ecKeyPair.public as ECPublicKey
        val jwkProvider = MapBackedJwkProvider(mapOf(kid to createEcJwk(kid, ecPublicKey)))
        val externalProviders = createExternalProviders(
            allowedAlgs = "RS256",
            jwkProviders = mapOf(externalIssuer to jwkProvider)
        )
        val resolver = JwtVerifierResolverImpl(
            internalIssuer,
            internalAudience,
            generateRsaKeyPair().public as RSAPublicKey,
            externalProviders
        )

        assertFailsWith<JwtUnsupportedKeyTypeException> {
            resolver.resolve(token)
        }
    }

    private fun createExternalProviders(
        allowedAlgs: String,
        jwkProviders: Map<String, JwkProvider>
    ): JwkExternalProviders {
        // Purpose: build external provider config identical to production parsing.
        // How: reuse createConfig with a map-based resolver and inject in-memory JWK providers.
        val config = JwkExternalProvidersImpl.createConfig(
            MapConfigResolver(
                mapOf(
                    "medatarun.auth.oidc.external.names" to "external",
                    "medatarun.auth.oidc.external.external.issuer" to externalIssuer,
                    "medatarun.auth.oidc.external.external.jwksUri" to jwksUri,
                    "medatarun.auth.oidc.external.external.audience" to externalAudience,
                    "medatarun.auth.oidc.external.external.algs" to allowedAlgs
                )
            ),
            internalIssuer
        )
        return JwtExternalProvidersForTests(config, jwkProviders)
    }

    private fun createRsaToken(
        issuer: String,
        audience: String,
        keyPair: KeyPair,
        kid: String
    ): String {
        // Purpose: create a valid RS256 token for resolver tests.
        // How: sign with the generated RSA key and include iss/aud/kid.
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withKeyId(kid)
            .sign(
                Algorithm.RSA256(
                    keyPair.public as RSAPublicKey,
                    keyPair.private as RSAPrivateKey
                )
            )
    }

    private fun createEcToken(
        issuer: String,
        audience: String,
        keyPair: KeyPair,
        kid: String
    ): String {
        // Purpose: create a valid ES256 token for resolver tests.
        // How: sign with the generated EC key and include iss/aud/kid.
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withKeyId(kid)
            .sign(
                Algorithm.ECDSA256(
                    keyPair.public as ECPublicKey,
                    keyPair.private as ECPrivateKey
                )
            )
    }

    private fun generateRsaKeyPair(): KeyPair {
        // Purpose: create ephemeral RSA keys for signing/verification.
        // How: generate a 2048-bit RSA key pair.
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        return generator.generateKeyPair()
    }

    private fun generateEcKeyPair(): KeyPair {
        // Purpose: create ephemeral EC keys for ES256 signing/verification.
        // How: generate a P-256 key pair using secp256r1.
        val generator = KeyPairGenerator.getInstance("EC")
        generator.initialize(ECGenParameterSpec("secp256r1"))
        return generator.generateKeyPair()
    }

    private fun createTokenWithoutAlg(issuer: String): String {
        // Purpose: craft a JWT missing the alg header.
        // How: build a raw token with a minimal header/payload and no signature.
        val header = base64Url("""{"typ":"JWT"}""")
        val payload = base64Url("""{"iss":"$issuer"}""")
        return "$header.$payload."
    }

    private fun createRsaJwk(kid: String, publicKey: RSAPublicKey): Jwk {
        // Purpose: construct an RSA JWK from a public key.
        // How: encode modulus/exponent into JWK attributes for Jwk.fromValues.
        val attributes = mutableMapOf<String, Any>()
        attributes["kid"] = kid
        attributes["kty"] = "RSA"
        attributes["alg"] = "RS256"
        attributes["n"] = base64Url(publicKey.modulus.toByteArray())
        attributes["e"] = base64Url(publicKey.publicExponent.toByteArray())
        return Jwk.fromValues(attributes)
    }

    private fun createEcJwk(kid: String, publicKey: ECPublicKey): Jwk {
        // Purpose: construct an EC JWK from a public key.
        // How: encode x/y coordinates with curve metadata for Jwk.fromValues.
        val attributes = mutableMapOf<String, Any>()
        attributes["kid"] = kid
        attributes["kty"] = "EC"
        attributes["alg"] = "ES256"
        attributes["crv"] = "P-256"
        attributes["x"] = base64Url(toUnsignedBytes(publicKey.w.affineX))
        attributes["y"] = base64Url(toUnsignedBytes(publicKey.w.affineY))
        return Jwk.fromValues(attributes)
    }

    private fun base64Url(value: String): String {
        // Purpose: encode small JSON fragments for JWT header/payload.
        // How: use URL-safe base64 without padding.
        return base64Url(value.toByteArray(Charsets.UTF_8))
    }

    private fun base64Url(bytes: ByteArray): String {
        // Purpose: produce JWT/JWK-compatible base64url.
        // How: encode with URL-safe base64 and remove padding.
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun toUnsignedBytes(value: BigInteger): ByteArray {
        // Purpose: satisfy JWK requirement for unsigned big-endian values.
        // How: remove leading sign byte when present.
        val bytes = value.toByteArray()
        if (bytes.isNotEmpty() && bytes[0].toInt() == 0) {
            return bytes.copyOfRange(1, bytes.size)
        }
        return bytes
    }

    private class JwtExternalProvidersForTests(
        private val config: ExternalOidcProvidersConfig,
        private val jwkProviders: Map<String, JwkProvider>
    ) : JwkExternalProviders {
        // Purpose: avoid network calls while keeping production config behavior.
        // How: return providers from an in-memory map and use config for issuer lookup.
        override fun findByIssuer(issuer: String): JwkProvider {
            val provider = jwkProviders[issuer]
            if (provider == null) {
                throw io.medatarun.auth.domain.JwtJwksUnknownExternalProvider(issuer)
            }
            return provider
        }

        override fun findExternalProvider(issuer: String): ExternalOidcProviderConfig {
            val provider = config.providers.firstOrNull { it.issuer == issuer }
            if (provider == null) {
                throw JwtUnknownIssuerException(issuer)
            }
            return provider
        }
    }

    private class MapBackedJwkProvider(
        private val jwks: Map<String, Jwk>,
        private val exceptions: Map<String, JwkException> = emptyMap()
    ) : JwkProvider {
        // Purpose: deterministically control JWKS responses per kid.
        // How: return a mapped JWK or throw a mapped exception for the requested key.
        override fun get(keyId: String): Jwk {
            val exception = exceptions[keyId]
            if (exception != null) {
                throw exception
            }
            val jwk = jwks[keyId]
            if (jwk == null) {
                throw SigningKeyNotFoundException("missing kid $keyId", null)
            }
            return jwk
        }
    }
}
