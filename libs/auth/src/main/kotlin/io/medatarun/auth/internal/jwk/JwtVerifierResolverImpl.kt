package io.medatarun.auth.internal.jwk

import com.auth0.jwk.InvalidPublicKeyException
import com.auth0.jwk.JwkException
import com.auth0.jwk.NetworkException
import com.auth0.jwk.SigningKeyNotFoundException
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import io.medatarun.auth.domain.*
import io.medatarun.auth.ports.exposed.JwtVerifierResolver
import org.slf4j.LoggerFactory
import java.security.PublicKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey

/**
 * Resolves verifiers by issuer so Ktor only has to delegate token verification.
 */
class JwtVerifierResolverImpl(
    private val internalIssuer: String,
    private val internalAudience: String,
    private val internalPublicKey: RSAPublicKey,
    private val externalJwkProviders: JwkExternalProviders
) : JwtVerifierResolver {
    private val logger = LoggerFactory.getLogger(JwtVerifierResolverImpl::class.java)

    override fun resolve(token: String): JWTVerifier {
        // Decode the token
        val tokenDecoded = decodeToken(token)

        // Get issuer from the token, we don't accept tokens without issuers
        val tokenIssuer = tokenDecoded.issuer ?: throw JwtMissingIssuerException()

        // Algorith in the token. This will fail if the algorithm is not
        // in our list of supported algorithms in our system
        val tokenAlg: JwtSupportedAlgorithm =
            JwtSupportedAlgorithm.valueOfKey(tokenDecoded.algorithm ?: throw JwtMissingAlgException())

        // Detect if it's from our own IdP or external
        val isInternal = tokenIssuer == internalIssuer

        // Adapt the strategy depending on if it is our own IdP or external
        // - internal: It's us, we know our key
        // - external: this will throw exception if the issuer is not in the list of declared providers
        val strategy = if (isInternal) {
            InternalProvider(internalAudience, internalPublicKey)
        } else {
            ExternalProvider(
                tokenIssuer,
                tokenDecoded,
                externalJwkProviders,
                tokenAlg
            )
        }

        if (!strategy.allowedAlgs.contains(tokenAlg)) {
            throw JwtUnsupportedAlgException(tokenAlg.key)
        }

        val publicKey = strategy.getPublicKey()

        val algorithm = buildAlgorithm(alg = tokenAlg, publicKey = publicKey)

        val baseVerifier = JWT.require(algorithm)
            .withIssuer(tokenIssuer)
            .let {
                if (strategy.audiences.isNotEmpty()) {
                    it.withAnyOfAudience(*strategy.audiences.toTypedArray())
                } else it
            }
            .build()
        return baseVerifier
    }

    sealed interface ProviderStrategy {
        val allowedAlgs: List<JwtSupportedAlgorithm>
        val audiences: List<String>
        fun getPublicKey(): PublicKey
    }

    class InternalProvider(private val internalAudience: String, private val internalPublicKey: PublicKey) :
        ProviderStrategy {
        override val allowedAlgs = JwtSupportedAlgorithm.entries.toList()
        override val audiences = listOf(internalAudience)
        override fun getPublicKey(): PublicKey {
            return internalPublicKey
        }
    }

    class ExternalProvider(
        private val issuer: String,
        private val decoded: DecodedJWT,
        private val externalJwkProviders: JwkExternalProviders,
        private val tokenAlg: JwtSupportedAlgorithm
    ) : ProviderStrategy {
        val cfg = externalJwkProviders.findExternalProvider(issuer)
        override val allowedAlgs = cfg.allowedAlgs
        override val audiences = cfg.audiences
        override fun getPublicKey(): PublicKey {
            val kid = decoded.keyId ?: throw JwtMissingKidException()
            val jwkProvider = externalJwkProviders.findByIssuer(issuer)
            val jwk = try {
                jwkProvider.get(kid)
            } catch (e: SigningKeyNotFoundException) {
                throw JwtUnknownKidException(kid, issuer)
            } catch (e: InvalidPublicKeyException) {
                throw JwtUnsupportedKeyTypeException()
            } catch (e: NetworkException) {
                throw JwtJwksFetchException(issuer, cfg.jwksUri)
            } catch (e: JwkException) {
                throw JwtJwksFetchException(issuer, cfg.jwksUri)
            }
            val jwkKey = jwk.publicKey

            // The key coming from the JWKS is generic (PublicKey).
            // The JWT algorithm requires a specific key type.
            // If the key type does not match the algorithm, the token must be rejected.
            return when (tokenAlg) {
                JwtSupportedAlgorithm.RS256 -> (jwkKey as? RSAPublicKey) ?: throw JwtUnsupportedKeyTypeException()
                JwtSupportedAlgorithm.ES256 -> (jwkKey as? ECPublicKey) ?: throw JwtUnsupportedKeyTypeException()
            }
        }
    }

    fun buildAlgorithm(
        alg: JwtSupportedAlgorithm,
        publicKey: PublicKey
    ): Algorithm {

        return when (alg) {
            JwtSupportedAlgorithm.RS256 -> Algorithm.RSA256(
                publicKey as? RSAPublicKey
                    ?: throw IllegalArgumentException("RS256 requires RSA key"),
                null
            )

            JwtSupportedAlgorithm.ES256 -> Algorithm.ECDSA256(
                publicKey as? ECPublicKey
                    ?: throw IllegalArgumentException("ES256 requires EC key"),
                null
            )
        }
    }

    private fun decodeToken(token: String): DecodedJWT {
        try {
            return JWT.decode(token)
        } catch (e: Exception) {
            logger.warn("Failed to decode JWT before verification.", e)
            throw JwtMalformedTokenException()
        }
    }


}
