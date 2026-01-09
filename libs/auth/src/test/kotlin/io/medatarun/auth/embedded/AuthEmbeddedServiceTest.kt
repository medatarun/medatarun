package io.medatarun.auth.embedded

import com.auth0.jwt.JWT
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.medatarun.auth.AuthExtension.Companion.DEFAULT_AUTH_CTX_DURATION_SECONDS
import io.medatarun.auth.domain.AuthEmbeddedBadCredentialsException
import io.medatarun.auth.domain.AuthEmbeddedJwtConfig
import io.medatarun.auth.domain.AuthEmbeddedUserAlreadyExistsException
import io.medatarun.auth.infra.AuthorizeStorageSQLite
import io.medatarun.auth.infra.DbConnectionFactoryImpl
import io.medatarun.auth.infra.UserStoreSQLite
import io.medatarun.auth.internal.*
import io.medatarun.auth.ports.exposed.*
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.auth.ports.needs.AuthorizeStorage
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import java.sql.Connection
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthEmbeddedServiceTest {

    @Nested
    inner class AdminTests {
        val env = TestInit()

        @Test
        fun `admin can log in`() {
            val token = env.oauthService.oidcLogin(env.adminUser, env.adminPassword)
            assertNotNull(token)
        }

        @Test
        fun `admin cannot log in with bad login`() {
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.oauthService.oidcLogin(env.adminUser + "--", env.adminPassword)
            }
        }

        @Test
        fun `admin cannot log in with bad password`() {
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.oauthService.oidcLogin(env.adminUser, env.adminPassword + "---")
            }
        }
    }


    @Nested
    inner class UserCreationTests {
        val env = TestInit()
        val johnUsername = "john.doe"
        val johnFullname = "John Doe"
        val johnPassword = "john.doe." + UUID.randomUUID().toString()

        fun createJohn() {
            env.userService.createEmbeddedUser(johnUsername, johnFullname, johnPassword, false)
        }

        @Test
        fun `can create john`() {
            createJohn()
            val token = env.oauthService.oidcLogin(johnUsername, johnPassword)
            assertNotNull(token)
        }

        @Test
        fun `can not create user with same login`() {
            createJohn()
            assertThrows<AuthEmbeddedUserAlreadyExistsException> {
                env.userService.createEmbeddedUser(johnUsername, "Other", "other.name." + UUID.randomUUID(), false)
            }
        }

        @Test
        fun `john cannot log in with bad login`() {
            createJohn()
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.oauthService.oidcLogin("$johnUsername--", johnPassword)
            }
        }

        @Test
        fun `john cannot log in with bad password`() {
            createJohn()
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.oauthService.oidcLogin(johnUsername, "$johnPassword---")
            }
        }

        @Test
        fun `john cannot log in with admin password`() {
            createJohn()
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.oauthService.oidcLogin(johnUsername, env.adminPassword)
            }
        }

        @Test
        fun `john cannot fake admin with its password`() {
            createJohn()
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.oauthService.oidcLogin(env.adminUser, johnPassword)
            }
        }

        @Test
        fun `john can change his password`() {
            createJohn()
            env.userService.changeOwnPassword(johnUsername, johnPassword, "$johnPassword.new")
            // Old password shall not work again
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.oauthService.oidcLogin(johnUsername, johnPassword)
            }
            // New password works
            env.oauthService.oidcLogin(johnUsername, "$johnPassword.new")
            // Didn't changed by mistake admin password
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.oauthService.oidcLogin(env.adminUser, "$johnPassword.new")
            }
            val adminToken = env.oauthService.oidcLogin(env.adminUser, env.adminPassword)
            assertNotNull(adminToken)
        }

        @Test
        fun `admin can change john password`() {
            createJohn()
            env.userService.changeUserPassword(johnUsername, "$johnPassword.new")
            // Old password shall not work again
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.oauthService.oidcLogin(johnUsername, johnPassword)
            }
            // New password works
            env.oauthService.oidcLogin(johnUsername, "$johnPassword.new")
            // Didn't changed by mistake admin password
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.oauthService.oidcLogin(env.adminUser, "$johnPassword.new")
            }
            val adminToken = env.oauthService.oidcLogin(env.adminUser, env.adminPassword)
            assertNotNull(adminToken)
        }

        @Test
        fun `admin can change disable john`() {
            createJohn()
            env.userService.disableUser(johnUsername)
            // login shall fail
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.oauthService.oidcLogin(johnUsername, johnPassword)
            }
            // Didn't changed by mistake admin login
            val adminToken = env.oauthService.oidcLogin(env.adminUser, env.adminPassword)
            assertNotNull(adminToken)
        }

        @Test
        fun `can not reuse login of disabled john`() {
            createJohn()
            env.userService.disableUser(johnUsername)
            assertThrows<AuthEmbeddedUserAlreadyExistsException> {
                env.userService.createEmbeddedUser(johnUsername, "Another User", "test." + UUID.randomUUID(), false)
            }
        }

        @Test
        fun `can change fullname`() {
            createJohn()
            fun extractFullname(token: OAuthTokenResponse): String {
                return JWT.decode(token.accessToken).getClaim("name").asString()
            }
            // Checks fullname before change
            val tokenBefore = env.oauthService.oidcLogin(johnUsername, johnPassword)
            assertEquals(johnFullname, extractFullname(tokenBefore))
            env.userService.changeUserFullname(johnUsername, johnFullname + "new")

            // Checks fullname after change
            val tokenAfter = env.oauthService.oidcLogin(johnUsername, johnPassword)
            assertEquals(johnFullname + "new", extractFullname(tokenAfter))

            // Make sure there are no side effects on other users (bad update directive or something like that)
            val tokenAdmin = env.oauthService.oidcLogin(env.adminUser, env.adminPassword)
            assertEquals(env.adminFullname, extractFullname(tokenAdmin))
        }

    }


    class TestInit {
        val userService: AuthEmbeddedUserService
        val oidcService: AuthEmbeddedOIDCService
        val oauthService: OAuthService
        val adminUser: String = "admin"
        val adminFullname: String = "Admin"
        val adminPassword: String = "admin." + UUID.randomUUID().toString()
        val dbConnectionFactory: DbConnectionFactoryImpl

        // Keeps connection alive until this class lifecycle ends
        private val dbConnectionKeeper: Connection

        init {
            val fs = Jimfs.newFileSystem(Configuration.unix())
            val home = fs.getPath("/opt/medatarun")
            Files.createDirectories(home)

            val cfgBootstrapSecretPath = home.resolve(AuthEmbeddedBootstrapSecret.DEFAULT_BOOTSTRAP_SECRET_PATH_NAME)
            val cfgKeyStorePath = home.resolve(AuthEmbeddedKeyRegistry.DEFAULT_KEYSTORE_PATH_NAME)
            this.dbConnectionFactory =
                DbConnectionFactoryImpl("file:test_${UUID.randomUUID()}?mode=memory&cache=shared")
            //this.dbConnectionFactory = DbConnectionFactoryImpl(":memory:")
            dbConnectionKeeper = dbConnectionFactory.getConnection()
            val authClock = ClockTester()
            val passwordEncryptionDefaultIterations = AuthEmbeddedPwd.DEFAULT_ITERATIONS_FOR_TESTS

            // -----------------------------------------------------------
            // Same as in extension, mutualization for later
            // -----------------------------------------------------------

            val userStorage = UserStoreSQLite(dbConnectionFactory)
            val authStorage: AuthorizeStorage = AuthorizeStorageSQLite(dbConnectionFactory)

            val userClaimsService = UserClaimsService()
            val authEmbeddedKeyRegistry = AuthEmbeddedKeyRegistryImpl(cfgKeyStorePath)
            val authEmbeddedKeys = authEmbeddedKeyRegistry.loadOrCreateKeys()

            val jwtCfg = AuthEmbeddedJwtConfig(
                issuer = "urn:medatarun:${authEmbeddedKeys.kid}",  // stable tant que tes fichiers sont là
                audience = "medatarun",
                ttlSeconds = 3600
            )

            val userService: AuthEmbeddedUserService = AuthEmbeddedUserServiceImpl(
                bootstrapDirPath = cfgBootstrapSecretPath,
                userStorage = userStorage,
                clock = authClock,
                passwordEncryptionIterations = passwordEncryptionDefaultIterations
            )

            val oauthService = OAuthServiceImpl(
                userService = userService,
                keys = authEmbeddedKeys,
                jwtConfig = jwtCfg,
                userClaimsService = userClaimsService
            )


            val oidcService: AuthEmbeddedOIDCService = AuthEmbeddedOIDCServiceImpl(
                oidcAuthorizeService = AuthEmbeddedOIDCAuthorizeService(
                    storage = authStorage,
                    clock = authClock,
                    authCtxDurationSeconds = DEFAULT_AUTH_CTX_DURATION_SECONDS
                ),
                userStorage = userStorage,
                oidcAuthCodeStorage = authStorage,
                userClaimsService = userClaimsService,
                oauthService = oauthService,
                authEmbeddedKeys = authEmbeddedKeys,
                jwtCfg = jwtCfg
            )

            // ----------------------------------------------------------------
            // End of initialization block
            // Back to the tests
            // ----------------------------------------------------------------

            this.userService = userService
            this.oidcService = oidcService
            this.oauthService = oauthService

            var bootstrapSecretKeeper: String = ""
            userService.loadOrCreateBootstrapSecret { bootstrapSecret -> bootstrapSecretKeeper = bootstrapSecret }
            userService.adminBootstrap(bootstrapSecretKeeper, adminUser, adminFullname, adminPassword)
        }


    }

    class ClockTester(var staticNow: Instant = Instant.now()) : AuthClock {
        override fun now(): Instant = staticNow
    }
}