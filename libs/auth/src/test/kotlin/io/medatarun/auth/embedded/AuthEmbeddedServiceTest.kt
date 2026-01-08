package io.medatarun.auth.embedded

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.medatarun.auth.domain.AuthEmbeddedBadCredentialsException
import io.medatarun.auth.domain.AuthEmbeddedUserAlreadyExistsException
import io.medatarun.auth.infra.DbConnectionFactoryImpl
import io.medatarun.auth.internal.AuthEmbeddedPwd
import io.medatarun.auth.internal.AuthEmbeddedServiceImpl
import io.medatarun.auth.internal.UserStoreSQLite
import io.medatarun.auth.ports.exposed.AuthEmbeddedBootstrapSecret
import io.medatarun.auth.ports.exposed.AuthEmbeddedKeyRegistry
import io.medatarun.auth.ports.exposed.AuthEmbeddedService
import io.medatarun.auth.ports.needs.AuthClock
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import java.sql.Connection
import java.time.Instant
import java.util.*
import kotlin.test.assertNotNull

class AuthEmbeddedServiceTest {

    @Nested
    inner class AdminTests {
        val env = TestInit()
        @Test
        fun `admin can log in`() {
            val token = env.service.oidcLogin(env.adminUser, env.adminPassword)
            assertNotNull(token)
        }

        @Test
        fun `admin cannot log in with bad login`() {
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.service.oidcLogin(env.adminUser + "--", env.adminPassword)
            }
        }

        @Test
        fun `admin cannot log in with bad password`() {
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.service.oidcLogin(env.adminUser, env.adminPassword + "---")
            }
        }
    }


    @Nested
    inner class UserCreationTests {
        val env = TestInit()
        val johnUsername = "john.doe"
        val johnFullname = "John Doe"
        val johnPassword = "john.doe."+UUID.randomUUID().toString()

        fun createJohn() {
            env.service.createEmbeddedUser(johnUsername, johnFullname, johnPassword, false)
        }

        @Test
        fun `can create john`() {
            createJohn()
            val token = env.service.oidcLogin(johnUsername, johnPassword)
            assertNotNull(token)
        }

        @Test
        fun `can not create user with same login`() {
            createJohn()
            assertThrows<AuthEmbeddedUserAlreadyExistsException> {
                env.service.createEmbeddedUser(johnUsername, "Other", "other.name."+UUID.randomUUID(), false)
            }
        }

        @Test
        fun `john cannot log in with bad login`() {
            createJohn()
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.service.oidcLogin("$johnUsername--", johnPassword)
            }
        }

        @Test
        fun `john cannot log in with bad password`() {
            createJohn()
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.service.oidcLogin(johnUsername, "$johnPassword---")
            }
        }
        @Test
        fun `john cannot log in with admin password`() {
            createJohn()
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.service.oidcLogin(johnUsername, env.adminPassword)
            }
        }
        @Test
        fun `john cannot fake admin with its password`() {
            createJohn()
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.service.oidcLogin(env.adminUser, johnPassword)
            }
        }

        @Test
        fun `john can change his password`() {
            createJohn()
            env.service.changeOwnPassword(johnUsername, johnPassword, "$johnPassword.new")
            // Old password shall not work again
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.service.oidcLogin(johnUsername, johnPassword)
            }
            // New password works
            env.service.oidcLogin(johnUsername, "$johnPassword.new")
            // Didn't changed by mistake admin password
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.service.oidcLogin(env.adminUser, "$johnPassword.new")
            }
            val adminToken = env.service.oidcLogin(env.adminUser, env.adminPassword)
            assertNotNull(adminToken)
        }

        @Test
        fun `admin can change john password`() {
            createJohn()
            env.service.changeUserPassword(johnUsername, "$johnPassword.new")
            // Old password shall not work again
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.service.oidcLogin(johnUsername, johnPassword)
            }
            // New password works
            env.service.oidcLogin(johnUsername, "$johnPassword.new")
            // Didn't changed by mistake admin password
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.service.oidcLogin(env.adminUser, "$johnPassword.new")
            }
            val adminToken = env.service.oidcLogin(env.adminUser, env.adminPassword)
            assertNotNull(adminToken)
        }

        @Test
        fun `admin can change disable john`() {
            createJohn()
            env.service.disableUser(johnUsername)
            // login shall fail
            assertThrows<AuthEmbeddedBadCredentialsException> {
                env.service.oidcLogin(johnUsername, johnPassword)
            }
            // Didn't changed by mistake admin login
            val adminToken = env.service.oidcLogin(env.adminUser, env.adminPassword)
            assertNotNull(adminToken)
        }

        @Test
        fun `can not reuse login of disabled john`() {
            createJohn()
            env.service.disableUser(johnUsername)
            assertThrows<AuthEmbeddedUserAlreadyExistsException> {
                env.service.createEmbeddedUser(johnUsername, "Another User", "test."+UUID.randomUUID(), false)
            }
        }

    }


    class TestInit {
        val service: AuthEmbeddedService
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
            val bootstrapDir = home.resolve(AuthEmbeddedBootstrapSecret.DEFAULT_BOOTSTRAP_SECRET_PATH_NAME)
            val keyStorePath = home.resolve(AuthEmbeddedKeyRegistry.DEFAULT_KEYSTORE_PATH_NAME)
            this.dbConnectionFactory =
                DbConnectionFactoryImpl("file:test_${UUID.randomUUID()}?mode=memory&cache=shared")
            //this.dbConnectionFactory = DbConnectionFactoryImpl(":memory:")
            dbConnectionKeeper = dbConnectionFactory.getConnection()
            val userStorage = UserStoreSQLite(dbConnectionFactory)
            val clock = ClockTester()
            this.service = AuthEmbeddedServiceImpl(
                bootstrapDirPath = bootstrapDir,
                keyStorePath = keyStorePath,
                userStorage = userStorage,
                clock = clock,
                passwordEncryptionIterations = AuthEmbeddedPwd.DEFAULT_ITERATIONS_FOR_TESTS
            )
            var bootstrapSecretKeeper: String = ""
            service.loadOrCreateBootstrapSecret { bootstrapSecret -> bootstrapSecretKeeper = bootstrapSecret }
            service.adminBootstrap(bootstrapSecretKeeper, adminUser, adminFullname, adminPassword)
        }


    }

    class ClockTester(var staticNow: Instant = Instant.now()) : AuthClock {
        override fun now(): Instant = staticNow
    }
}