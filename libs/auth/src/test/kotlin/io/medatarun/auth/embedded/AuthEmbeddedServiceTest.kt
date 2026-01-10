package io.medatarun.auth.embedded

import com.auth0.jwt.JWT
import io.medatarun.auth.domain.AuthUnauthorizedException
import io.medatarun.auth.domain.UserAlreadyExistsException
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.ports.exposed.OAuthTokenResponse
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthEmbeddedServiceTest {

    @Nested
    inner class AdminTests {
        val env = AuthEnvTest()

        @Test
        fun `admin can log in`() {
            val token = env.oauthService.oauthLogin(env.adminUser, env.adminPassword)
            assertNotNull(token)
        }

        @Test
        fun `admin cannot log in with bad login`() {
            assertThrows<AuthUnauthorizedException> {
                env.oauthService.oauthLogin(env.adminUser + "--", env.adminPassword)
            }
        }

        @Test
        fun `admin cannot log in with bad password`() {
            assertThrows<AuthUnauthorizedException> {
                env.oauthService.oauthLogin(env.adminUser, env.adminPassword + "---")
            }
        }
    }


    @Nested
    inner class UserCreationTests {
        val env = AuthEnvTest()
        val johnUsername = "john.doe"
        val johnFullname = "John Doe"
        val johnPassword = "john.doe." + UUID.randomUUID().toString()

        fun createJohn() {
            env.userService.createEmbeddedUser(johnUsername, johnFullname, johnPassword, false)
        }

        @Test
        fun `can create john`() {
            createJohn()
            val token = env.oauthService.oauthLogin(johnUsername, johnPassword)
            assertNotNull(token)
        }

        @Test
        fun `can not create user with same login`() {
            createJohn()
            assertThrows<UserAlreadyExistsException> {
                env.userService.createEmbeddedUser(johnUsername, "Other", "other.name." + UUID.randomUUID(), false)
            }
        }

        @Test
        fun `john cannot log in with bad login`() {
            createJohn()
            assertThrows<AuthUnauthorizedException> {
                env.oauthService.oauthLogin("$johnUsername--", johnPassword)
            }
        }

        @Test
        fun `john cannot log in with bad password`() {
            createJohn()
            assertThrows<AuthUnauthorizedException> {
                env.oauthService.oauthLogin(johnUsername, "$johnPassword---")
            }
        }

        @Test
        fun `john cannot log in with admin password`() {
            createJohn()
            assertThrows<AuthUnauthorizedException> {
                env.oauthService.oauthLogin(johnUsername, env.adminPassword)
            }
        }

        @Test
        fun `john cannot fake admin with its password`() {
            createJohn()
            assertThrows<AuthUnauthorizedException> {
                env.oauthService.oauthLogin(env.adminUser, johnPassword)
            }
        }

        @Test
        fun `john can change his password`() {
            createJohn()
            env.userService.changeOwnPassword(johnUsername, johnPassword, "$johnPassword.new")
            // Old password shall not work again
            assertThrows<AuthUnauthorizedException> {
                env.oauthService.oauthLogin(johnUsername, johnPassword)
            }
            // New password works
            env.oauthService.oauthLogin(johnUsername, "$johnPassword.new")
            // Didn't changed by mistake admin password
            assertThrows<AuthUnauthorizedException> {
                env.oauthService.oauthLogin(env.adminUser, "$johnPassword.new")
            }
            val adminToken = env.oauthService.oauthLogin(env.adminUser, env.adminPassword)
            assertNotNull(adminToken)
        }

        @Test
        fun `admin can change john password`() {
            createJohn()
            env.userService.changeUserPassword(johnUsername, "$johnPassword.new")
            // Old password shall not work again
            assertThrows<AuthUnauthorizedException> {
                env.oauthService.oauthLogin(johnUsername, johnPassword)
            }
            // New password works
            env.oauthService.oauthLogin(johnUsername, "$johnPassword.new")
            // Didn't changed by mistake admin password
            assertThrows<AuthUnauthorizedException> {
                env.oauthService.oauthLogin(env.adminUser, "$johnPassword.new")
            }
            val adminToken = env.oauthService.oauthLogin(env.adminUser, env.adminPassword)
            assertNotNull(adminToken)
        }

        @Test
        fun `admin can change disable john`() {
            createJohn()
            env.userService.disableUser(johnUsername)
            // login shall fail
            assertThrows<AuthUnauthorizedException> {
                env.oauthService.oauthLogin(johnUsername, johnPassword)
            }
            // Didn't changed by mistake admin login
            val adminToken = env.oauthService.oauthLogin(env.adminUser, env.adminPassword)
            assertNotNull(adminToken)
        }

        @Test
        fun `can not reuse login of disabled john`() {
            createJohn()
            env.userService.disableUser(johnUsername)
            assertThrows<UserAlreadyExistsException> {
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
            val tokenBefore = env.oauthService.oauthLogin(johnUsername, johnPassword)
            assertEquals(johnFullname, extractFullname(tokenBefore))
            env.userService.changeUserFullname(johnUsername, johnFullname + "new")

            // Checks fullname after change
            val tokenAfter = env.oauthService.oauthLogin(johnUsername, johnPassword)
            assertEquals(johnFullname + "new", extractFullname(tokenAfter))

            // Make sure there are no side effects on other users (bad update directive or something like that)
            val tokenAdmin = env.oauthService.oauthLogin(env.adminUser, env.adminPassword)
            assertEquals(env.adminFullname, extractFullname(tokenAdmin))
        }

    }


}