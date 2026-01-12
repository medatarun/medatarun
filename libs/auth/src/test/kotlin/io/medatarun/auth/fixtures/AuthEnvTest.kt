package io.medatarun.auth.fixtures

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.medatarun.auth.AuthExtension
import io.medatarun.auth.domain.*
import io.medatarun.auth.infra.ActorStorageSQLite
import io.medatarun.auth.infra.DbConnectionFactoryImpl
import io.medatarun.auth.infra.OidcStorageSQLite
import io.medatarun.auth.infra.UserStorageSQLite
import io.medatarun.auth.internal.*
import io.medatarun.auth.ports.exposed.*
import io.medatarun.auth.ports.needs.OidcStorage
import java.nio.file.Files
import java.sql.Connection
import java.util.*
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
    private val overrideJwtConfig: JwtConfig? = null,
    private val createAdmin: Boolean = true
) {



    val userService: UserService
    val oidcService: OidcService
    val actorService: ActorService
    val bootstrapSecretLifecycle: BootstrapSecretLifecycle

    // We provide impl and not only the interface because some tests are trick and focus only
    // on JWT Generation. Could be better but for now it's ok.
    val oauthService: OAuthServiceImpl

    val adminUsername: Username = Username("admin")
    val adminFullname: Fullname = Fullname("Admin")
    val adminPassword: PasswordClear = PasswordClear("admin." + UUID.randomUUID().toString())
    val dbConnectionFactory: DbConnectionFactoryImpl
    val jwtKeyMaterial: JwtKeyMaterial
    val jwtConfig: JwtConfig
    var bootstrapSecretKeeper = ""

    // Keeps connection alive until this class lifecycle ends
    private val dbConnectionKeeper: Connection

    init {

        // Virtual filesystem in memory
        val fs = Jimfs.newFileSystem(Configuration.unix())
        val home = fs.getPath("/opt/medatarun")
        Files.createDirectories(home)

        val cfgBootstrapSecretPath =
            home.resolve(BootstrapSecretLifecycle.DEFAULT_BOOTSTRAP_SECRET_PATH_NAME)
        val cfgKeyStorePath = home.resolve(JwtSigninKeyRegistry.DEFAULT_KEYSTORE_PATH_NAME)

        // In memory database. Be sure to keep one connection alive during the lifecycle
        // of any instance of this class. Using UUIDs to name in memory databases or else
        // SQLite will reuse existing bases across tests.
        this.dbConnectionFactory =
            DbConnectionFactoryImpl("file:test_${UUID.randomUUID()}?mode=memory&cache=shared")
        dbConnectionKeeper = dbConnectionFactory.getConnection()

        // Fake clock that always give the same point in time. Used to tests instant.now()
        val authClock = ClockTester()

        // Reduce number of iterations needed for password encryption (from 31_0000 to 1000)
        val passwordEncryptionDefaultIterations = UserPasswordEncrypter.DEFAULT_ITERATIONS_FOR_TESTS

        // No bootstrap secret in configuration, it will be random
        val cfgBootstrapSecret = null

        // -----------------------------------------------------------
        // Same as in extension, mutualization for later
        // -----------------------------------------------------------

        val userStorage = UserStorageSQLite(dbConnectionFactory)
        val authStorage: OidcStorage = OidcStorageSQLite(dbConnectionFactory)
        val actorStorage = ActorStorageSQLite(dbConnectionFactory)

        val actorClaimsAdapter = ActorClaimsAdapter()
        val authEmbeddedKeyRegistry = JwtSigninKeyRegistryImpl(cfgKeyStorePath)
        val authEmbeddedKeys = authEmbeddedKeyRegistry.loadOrCreateKeys()

        val jwtCfg = overrideJwtConfig ?: JwtConfig(
            issuer = "urn:medatarun:${authEmbeddedKeys.kid}",  // stable tant que tes fichiers sont là
            audience = "medatarun",
            ttlSeconds = 3600
        )
        val bootstrapper = BootstrapSecretLifecycleImpl(cfgBootstrapSecretPath, cfgBootstrapSecret)

        val actorService = ActorServiceImpl(actorStorage, authClock)

        val userService: UserService = UserServiceImpl(
            userStorage = userStorage,
            clock = authClock,
            passwordEncryptionIterations = passwordEncryptionDefaultIterations,
            bootstrapper = bootstrapper,
            userEvents = UserServiceEventsActorProvisioning(actorService, jwtCfg.issuer)
        )

        val oauthService = OAuthServiceImpl(
            userService = userService,
            keys = authEmbeddedKeys,
            jwtConfig = jwtCfg,
            actorClaimsAdapter = actorClaimsAdapter,
            actorService = actorService
        )


        val oidcService: OidcService = OidcServiceImpl(
            oidcAuthCodeStorage = authStorage,
            actorClaimsAdapter = actorClaimsAdapter,
            oauthService = oauthService,
            authEmbeddedKeys = authEmbeddedKeys,
            jwtCfg = jwtCfg,
            clock = authClock,
            actorService = actorService,
            authCtxDurationSeconds = AuthExtension.DEFAULT_AUTH_CTX_DURATION_SECONDS
        )

        // ----------------------------------------------------------------
        // End of initialization block
        // Back to the tests
        // ----------------------------------------------------------------

        this.userService = userService
        this.oidcService = oidcService
        this.oauthService = oauthService
        this.jwtKeyMaterial = authEmbeddedKeys
        this.jwtConfig = jwtCfg
        this.actorService = actorService
        this.bootstrapSecretLifecycle = bootstrapper
        this.bootstrapSecretKeeper = ""

        userService.loadOrCreateBootstrapSecret { bootstrapSecret -> bootstrapSecretKeeper = bootstrapSecret }

        if (createAdmin) {
            userService.adminBootstrap(bootstrapSecretKeeper, adminUsername, adminFullname, adminPassword)
        }
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

}
