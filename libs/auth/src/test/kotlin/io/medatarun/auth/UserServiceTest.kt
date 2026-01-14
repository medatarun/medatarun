package io.medatarun.auth

import com.auth0.jwt.JWT
import io.medatarun.auth.domain.AuthUnauthorizedException
import io.medatarun.auth.domain.UserAlreadyExistsException
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.ports.exposed.OAuthTokenResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserServiceTest {

    val johnUsername = Username("john.doe")
    val johnFullname = Fullname("John Doe")
    val johnPassword = PasswordClear("john.doe." + UUID.randomUUID().toString())


    private fun createJohn(env: AuthEnvTest) {
        env.userService.createEmbeddedUser(johnUsername, johnFullname, johnPassword, false)
    }

    @Test
    fun `can create john`() {
        val env = AuthEnvTest()
        createJohn(env)
        val token = env.oauthService.oauthLogin(johnUsername, johnPassword)
        assertNotNull(token)
    }

    @Test
    fun `can not create user with same login`() {
        val env = AuthEnvTest()
        createJohn(env)
        assertThrows<UserAlreadyExistsException> {
            env.userService.createEmbeddedUser(
                johnUsername, Fullname("Other"),
                PasswordClear("other.name." + UUID.randomUUID()), false
            )
        }
    }

    @Test
    fun `john cannot log in with bad login`() {
        val env = AuthEnvTest()
        createJohn(env)
        assertThrows<AuthUnauthorizedException> {
            env.oauthService.oauthLogin(Username(johnUsername.value + "--"), johnPassword)
        }
    }

    @Test
    fun `john cannot log in with bad password`() {
        val env = AuthEnvTest()
        createJohn(env)
        assertThrows<AuthUnauthorizedException> {
            env.oauthService.oauthLogin(johnUsername, PasswordClear(johnPassword.value + "---"))
        }
    }

    @Test
    fun `john cannot log in with admin password`() {
        val env = AuthEnvTest()
        createJohn(env)
        assertThrows<AuthUnauthorizedException> {
            env.oauthService.oauthLogin(johnUsername, env.adminPassword)
        }
    }

    @Test
    fun `john cannot fake admin with its password`() {
        val env = AuthEnvTest()
        createJohn(env)
        assertThrows<AuthUnauthorizedException> {
            env.oauthService.oauthLogin(env.adminUsername, johnPassword)
        }
    }

    @Test
    fun `john can change his password`() {
        val env = AuthEnvTest()
        createJohn(env)
        val newJohnPassword = PasswordClear(johnPassword.value + ".new")
        env.userService.changeOwnPassword(johnUsername, johnPassword, newJohnPassword)
        // Old password shall not work again
        assertThrows<AuthUnauthorizedException> {
            env.oauthService.oauthLogin(johnUsername, johnPassword)
        }
        // New password works
        env.oauthService.oauthLogin(johnUsername, newJohnPassword)
        // Didn't changed by mistake admin password
        assertThrows<AuthUnauthorizedException> {
            env.oauthService.oauthLogin(env.adminUsername, newJohnPassword)
        }
        val adminToken = env.oauthService.oauthLogin(env.adminUsername, env.adminPassword)
        assertNotNull(adminToken)
    }

    @Test
    fun `admin can change john password`() {
        val env = AuthEnvTest()
        createJohn(env)
        val newJohnPassword = PasswordClear(johnPassword.value + ".new")
        env.userService.changeUserPassword(johnUsername, newJohnPassword)
        // Old password shall not work again
        assertThrows<AuthUnauthorizedException> {
            env.oauthService.oauthLogin(johnUsername, johnPassword)
        }
        // New password works
        env.oauthService.oauthLogin(johnUsername, newJohnPassword)
        // Didn't changed by mistake admin password
        assertThrows<AuthUnauthorizedException> {
            env.oauthService.oauthLogin(env.adminUsername, newJohnPassword)
        }
        val adminToken = env.oauthService.oauthLogin(env.adminUsername, env.adminPassword)
        assertNotNull(adminToken)
    }

    @Test
    fun `admin can change disable john`() {
        val env = AuthEnvTest()
        createJohn(env)
        env.userService.disableUser(johnUsername)
        // login shall fail
        assertThrows<AuthUnauthorizedException> {
            env.oauthService.oauthLogin(johnUsername, johnPassword)
        }
        // Didn't changed by mistake admin login
        val adminToken = env.oauthService.oauthLogin(env.adminUsername, env.adminPassword)
        assertNotNull(adminToken)
    }

    @Test
    fun `can not reuse login of disabled john`() {
        val env = AuthEnvTest()
        createJohn(env)
        env.userService.disableUser(johnUsername)
        assertThrows<UserAlreadyExistsException> {
            env.userService.createEmbeddedUser(
                johnUsername, Fullname("Another User"),
                PasswordClear("test." + UUID.randomUUID()), false
            )
        }
    }

    @Test
    fun `can change fullname`() {
        val env = AuthEnvTest()
        createJohn(env)
        fun extractFullname(token: OAuthTokenResponse): String {
            return JWT.decode(token.accessToken).getClaim("name").asString()
        }
        // Checks fullname before change
        val tokenBefore = env.oauthService.oauthLogin(johnUsername, johnPassword)
        assertEquals(johnFullname, Fullname(extractFullname(tokenBefore)))
        env.userService.changeUserFullname(johnUsername, Fullname(johnFullname.value + "new"))

        // Checks fullname after change
        val tokenAfter = env.oauthService.oauthLogin(johnUsername, johnPassword)
        assertEquals(Fullname(johnFullname.value + "new"), Fullname(extractFullname(tokenAfter)))

        // Make sure there are no side effects on other users (bad update directive or something like that)
        val tokenAdmin = env.oauthService.oauthLogin(env.adminUsername, env.adminPassword)
        assertEquals(env.adminFullname, Fullname(extractFullname(tokenAdmin)))
    }


}