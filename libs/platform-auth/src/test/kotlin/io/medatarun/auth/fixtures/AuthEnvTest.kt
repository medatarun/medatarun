package io.medatarun.auth.fixtures

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.google.common.jimfs.Jimfs
import io.medatarun.actions.ActionsExtension
import io.medatarun.auth.AuthExtension
import io.medatarun.auth.AuthExtensionConfig
import io.medatarun.auth.domain.jwt.JwtConfig
import io.medatarun.auth.domain.jwt.JwtKeyMaterial
import io.medatarun.auth.domain.oidc.OidcAuthorizeRequest
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.internal.oidc.AuthClientRegistry
import io.medatarun.auth.internal.users.UserPasswordEncrypter
import io.medatarun.auth.ports.exposed.*
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.DbMigrationChecker
import io.medatarun.platform.db.PlatformStorageDbExtension
import io.medatarun.platform.db.postgresql.PlatformStorageDbPostgresqlExtension
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension
import io.medatarun.platform.db.testkit.TestDbConfig
import io.medatarun.platform.kernel.*
import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionStringBased
import io.medatarun.security.SecurityExtension
import io.medatarun.security.SecurityPermissionsProvider
import io.medatarun.types.TypeSystemExtension
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration-like test environment for Auth related tests.
 *
 * Creates a boostrap secret, a database, and user / oidc / oauth services.
 *
 * Uses JimFS as a virtual filesystem for storing files.
 *
 * A temporary in memory database is created for storages.
 *
 * An admin user is created automatically.
 *
 * Initialization is very similar to what is done in [AuthExtension] but with some tweaks for speed
 * (including the number of iterations for password encryption, or else tests are too slow).
 */
class AuthEnvTest(
    private val createAdmin: Boolean = true,
    private val otherPermissions: Set<TestOtherPermission> = emptySet(),
    private val extraProps: Map<String, String> = emptyMap(),
    val publicBaseUrl: URI = URI("https://auth.example.test")
) {

    val userService: UserService
    val oidcService: OidcService
    val actorService: ActorService
    val bootstrapSecretLifecycle: BootstrapSecretLifecycle

    // We provide impl and not only the interface because some tests are trick and focus only
    // on JWT Generation. Could be better but for now it's ok.
    val oauthService: OAuthService

    val adminUsername: Username = Username("admin")
    val adminFullname: Fullname = Fullname("Admin")
    val adminPassword: PasswordClear = PasswordClear("admin." + UuidUtils.generateV4String())

    val jwtKeyMaterial: JwtKeyMaterial
    val jwtConfig: JwtConfig
    var bootstrapSecretKeeper = ""

    val config = MedatarunConfig.createTempConfig(
        fs = Jimfs.newFileSystem(),
        appDir = "/opt/medatarun",
        props = TestDbConfig().testDatabaseProperties(extraProps),
        publicBaseURL = publicBaseUrl
    )

    // Fake clock that always give the same point in time. Used to tests instant.now()
    val authClockTests = ClockTester()

    val runtime = PlatformBuilder(
        config,
        listOf(
            TypeSystemExtension(),
            ActionsExtension(),
            SecurityExtension(),
            PlatformStorageDbExtension(),
            PlatformStorageDbSqliteExtension(),
            PlatformStorageDbPostgresqlExtension(),
            AuthExtension(object : AuthExtensionConfig {
                override val authClock: AuthClock
                    get() = authClockTests

                // Reduce number of iterations needed for password encryption (from 31_0000 to 1000)
                override val passwordEncryptionDefaultIterations: Int
                    get() = UserPasswordEncrypter.DEFAULT_ITERATIONS_FOR_TESTS
            }),
            TestOtherPermissionsExtension(otherPermissions)
        )
    ).buildAndStart()

    val dbMigrationChecker = runtime.services.getService<DbMigrationChecker>()


    init {
        // ----------------------------------------------------------------
        // End of initialization block
        // Back to the tests
        // ----------------------------------------------------------------

        this.userService = runtime.services.getService<UserService>()
        this.oidcService = runtime.services.getService<OidcService>()
        this.oauthService = runtime.services.getService<OAuthService>()
        this.actorService = runtime.services.getService<ActorService>()
        this.jwtKeyMaterial = runtime.services.getService<JwtKeyMaterial>()
        this.jwtConfig = runtime.services.getService<JwtConfig>()

        this.bootstrapSecretLifecycle = runtime.services.getService<BootstrapSecretLifecycle>()
        this.bootstrapSecretKeeper = ""

        this.userService.loadOrCreateBootstrapSecret { bootstrapSecret -> bootstrapSecretKeeper = bootstrapSecret }

        if (createAdmin) {
            this.userService.adminBootstrap(bootstrapSecretKeeper, adminUsername, adminFullname, adminPassword)
        }
    }

    /**
     * Creates an authorize request with predefined parameters
     */
    fun buildAuthorizeRequest(
        responseType: String? = "code",
        clientId: String? = AuthClientRegistry.oidcInternalClientId,
        redirectUri: String? = publicBaseUrl.resolve("/authentication-callback").toString(),
        scope: String? = "openid",
        state: String? = "state-123",
        codeChallenge: String? = "challenge-123",
        codeChallengeMethod: String? = "S256",
        nonce: String? = "nonce-123"
    ): OidcAuthorizeRequest {
        return OidcAuthorizeRequest(
            responseType = responseType,
            clientId = clientId,
            redirectUri = redirectUri,
            scope = scope,
            state = state,
            codeChallenge = codeChallenge,
            codeChallengeMethod = codeChallengeMethod,
            nonce = nonce
        )
    }

    fun verifyToken(
        token: String,
        expectedSub: String,
        expectedIssuer: String = jwtConfig.issuer,
        expectedAudience: String = jwtConfig.audience,
        expectedClaims: Map<String, Any?> = emptyMap(),
        keyMaterial: JwtKeyMaterial = jwtKeyMaterial
    ): DecodedJWT {
        val algorithm = Algorithm.RSA256(keyMaterial.publicKey, keyMaterial.privateKey)
        val verifier = JWT.require(algorithm)
            .withIssuer(expectedIssuer)
            .withAudience(expectedAudience)
            .withSubject(expectedSub)
            .build()

        val decodedJWT = verifier.verify(token)

        expectedClaims.forEach { (key, value) ->
            val claim = decodedJWT.getClaim(key)
            when (value) {
                is String -> assertEquals(value, claim.asString(), "Claim $key mismatch")
                is Boolean -> assertEquals(value, claim.asBoolean(), "Claim $key mismatch")
                is Int -> assertEquals(value, claim.asInt(), "Claim $key mismatch")
                is Long -> assertEquals(value, claim.asLong(), "Claim $key mismatch")
                is Double -> assertEquals(value, claim.asDouble(), "Claim $key mismatch")
                null -> assertTrue(claim.isNull, "Claim $key should be null")
                else -> assertEquals(value.toString(), claim.asString(), "Claim $key mismatch (stringified)")
            }
        }

        return decodedJWT
    }

    /**
     * Test extension that declares new permissions, like other modules
     * would do. This is used to test that permissions from all modules
     * are correctly contributed and seen by the auth module.
     */
    class TestOtherPermissionsExtension(val otherPermissions: Set<TestOtherPermission>) : MedatarunExtension {
        override val id: ExtensionId = "other-roles"
        override fun initContributions(ctx: MedatarunExtensionCtx) {
            ctx.registerContribution(
                SecurityPermissionsProvider::class,
                TestOtherSecurityPermissionsProvider(otherPermissions)
            )
        }
    }

    /**
     * A fake permission provider with a list of predefined permissions
     * coming from the test area
     */
    class TestOtherSecurityPermissionsProvider(val otherPermissions: Set<TestOtherPermission>) : SecurityPermissionsProvider {
        override fun getPermissions(): List<AppPermission> = otherPermissions.toList()
    }

    class TestOtherPermission(override val key: String): AppPermission
}
