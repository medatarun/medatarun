package io.medatarun.auth.internal

import com.auth0.jwk.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import io.medatarun.auth.domain.*
import io.medatarun.auth.domain.oidc.ExternalOidcProviderConfig
import io.medatarun.auth.ports.exposed.JwtVerifierResolver
import org.slf4j.LoggerFactory
import java.security.interfaces.RSAPublicKey

/**
 * Resolves verifiers by issuer so Ktor only has to delegate token verification.
 */
class JwtVerifierResolverImpl(
    private val internalIssuer: String,
    private val internalAudience: String,
    private val internalPublicKey: RSAPublicKey,
    private val externalProviders: List<ExternalOidcProviderConfig>,
    private val externalJwkProviders: Map<String, JwkProvider>
) : JwtVerifierResolver {
    private val logger = LoggerFactory.getLogger(JwtVerifierResolverImpl::class.java)
    private val internalAllowedAlgs = listOf("RS256")

    override fun resolve(token: String): JWTVerifier {
        val decoded = decodeToken(token)
        val issuer = decoded.issuer ?: throw JwtMissingIssuerException()
        val audience = decoded.audience
        if (audience == null || audience.isEmpty()) {
            throw JwtMissingAudienceException()
        }
        val alg = decoded.algorithm ?: throw JwtMissingAlgException()

        val isInternal = issuer == internalIssuer
        val externalProvider = if (isInternal) null else findExternalProvider(issuer)
        val allowedAlgs = if (isInternal) internalAllowedAlgs else externalProvider!!.allowedAlgs
        val audiences = if (isInternal) listOf(internalAudience) else externalProvider!!.audiences

        if (!allowedAlgs.contains(alg)) {
            throw JwtUnsupportedAlgException(alg)
        }

        val publicKey = if (isInternal) {
            internalPublicKey
        } else {
            val kid = decoded.keyId ?: throw JwtMissingKidException()
            val jwkProvider = externalJwkProviders[issuer]
                ?: throw JwtJwksFetchException(issuer, externalProvider!!.jwksUri)
            val jwk = try {
                jwkProvider.get(kid)
            } catch (e: SigningKeyNotFoundException) {
                throw JwtUnknownKidException(kid, issuer)
            } catch (e: InvalidPublicKeyException) {
                throw JwtUnsupportedKeyTypeException()
            } catch (e: NetworkException) {
                throw JwtJwksFetchException(issuer, externalProvider.jwksUri)
            } catch (e: JwkException) {
                throw JwtJwksFetchException(issuer, externalProvider.jwksUri)
            }
            val jwkKey = jwk.publicKey
            if (jwkKey is RSAPublicKey) {
                jwkKey
            } else {
                throw JwtUnsupportedKeyTypeException()
            }
        }

        val algorithm = Algorithm.RSA256(publicKey, null)
        val baseVerifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build()
        return AudienceCheckingJwtVerifier(baseVerifier, audiences)
    }

    private fun decodeToken(token: String): DecodedJWT {
        try {
            return JWT.decode(token)
        } catch (e: Exception) {
            logger.warn("Failed to decode JWT before verification.", e)
            throw JwtMalformedTokenException()
        }
    }

    private fun findExternalProvider(issuer: String): ExternalOidcProviderConfig {
        val provider = externalProviders.firstOrNull { it.issuer == issuer }
        if (provider == null) {
            throw JwtUnknownIssuerException(issuer)
        }
        return provider
    }

    private class AudienceCheckingJwtVerifier(
        private val delegate: JWTVerifier,
        private val expectedAudiences: List<String>
    ) : JWTVerifier {

        override fun verify(token: String): DecodedJWT {
            val verified = try {
                delegate.verify(token)
            } catch (e: JWTVerificationException) {
                throw JwtSignatureInvalidException()
            }
            verifyAudience(verified.audience)
            return verified
        }

        override fun verify(jwt: DecodedJWT): DecodedJWT {
            val verified = try {
                delegate.verify(jwt)
            } catch (e: JWTVerificationException) {
                throw JwtSignatureInvalidException()
            }
            verifyAudience(verified.audience)
            return verified
        }

        private fun verifyAudience(audience: List<String>?) {
            if (audience == null || audience.isEmpty()) {
                throw JwtMissingAudienceException()
            }
            val matched = audience.any { expectedAudiences.contains(it) }
            if (!matched) {
                throw JwtAudienceMismatchException(expectedAudiences)
            }
        }
    }
}

