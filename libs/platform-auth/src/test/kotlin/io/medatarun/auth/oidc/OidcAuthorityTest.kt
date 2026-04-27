package io.medatarun.auth.oidc

import io.medatarun.auth.domain.ConfigProperties
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.internal.jwk.JwtVerifierResolverImpl
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@EnableDatabaseTests
class OidcAuthorityTest {


    @Test
    fun `oidcIssuer is fixed`() {
        val env = AuthEnvTest()
        assertEquals(env.jwtConfig.issuer, env.oidcService.oidcIssuer())
    }

    @Test
    fun `jwtVerifierResolver is the one we tested`() {
        val env = AuthEnvTest()
        // just checks that the JWTVerifier is the one we implement
        // The verifier is complex and tested independently in its own
        // test suite
        //
        // We also check that we didn't mix bad parameters when
        // configuring it because it's a very sensitive part
        val resolver = env.oidcService.jwtVerifierResolver()
        assertIs<JwtVerifierResolverImpl>(resolver)
        assertEquals(env.jwtConfig.issuer, resolver.internalIssuer)
        assertEquals(env.jwtConfig.audience, resolver.internalAudience)
        assertEquals(env.jwtKeyMaterial.publicKey, resolver.internalPublicKey)

    }

    @Test
    fun `oidcAuthority and oidcClientId use provider config when defined`() {
        /*
         * Goal: Prefer configured authority and client id over the public base URL.
         * Why: External IdP configuration must override the default internal endpoints.
         * How: Build a service with OidcProviderConfig and verify authority/clientId values.
         */
        val configuredAuthority = URI("https://issuer.example.test")
        val configuredClientId = "client-oidc"

        val env = AuthEnvTest(
            extraProps = mapOf(
                ConfigProperties.UIOidcAuthority.key to configuredAuthority.toString(),
                ConfigProperties.UiOidcClientId.key to configuredClientId
            ),
            publicBaseUrl = URI("https://public.example.test")
        )

        val service = env.oidcService

        val authority = service.oidcAuthority()
        assertEquals(configuredAuthority, authority)
        assertEquals(configuredClientId, service.oidcClientId())
    }


}