package io.medatarun.auth.actions

import io.medatarun.auth.domain.*
import io.medatarun.auth.fixtures.AuthActionEnvTest
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.UserService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class AuthActionsTest {

    @Test
    fun `bootstrap called`() {
        val env = AuthActionEnvTest(createAdmin = false)
        val username = Username("admin")
        val password = PasswordClear("admin.0123456789")
        val token = env.dispatch(
            AuthAction.AdminBootstrap(
                secret = env.env.bootstrapSecretKeeper,
                username = username,
                password = password,
                fullname = Fullname("Admin")
            )
        )
        assertDoesNotThrow {
            env.env.verifyToken(token.accessToken, expectedSub = "admin")
        }

        val userService = env.getService(UserService::class)
        val user = userService.loginUser(username, password)
        assertEquals(username, user.login)

        // Test user exists in Actors
        val actorService = env.getService(ActorService::class)
        val actor = actorService.findByIssuerAndSubjectOptional(env.env.oidcService.oidcIssuer(), user.login.value)
        assertNotNull(actor)
        assertEquals(1, actor.roles.size)
        assertTrue(actor.roles.any{it.key == ActorRole.ADMIN.key})
    }
    @Test
    fun `create user called`() {
        val env = AuthActionEnvTest()
        val username = Username("john.doe")
        val password = PasswordClear("john.doe.0123456789")
        val fullname = Fullname("John Doe")
        env.asAdmin()
        env.dispatch(
            AuthAction.CreateUser(
                username = username,
                password = password,
                fullname = fullname,
                admin = false
            )
        )
        val user = env.getService(UserService::class).loginUser(username, password)
        assertFalse(user.admin)
        assertEquals(username, user.login)
        assertEquals(fullname, user.fullname)
    }

    @Test
    fun `login user called`() {
        val env = AuthActionEnvTest()
        val username = Username("john.doe")
        val password = PasswordClear("john.doe.0123456789")
        val fullname = Fullname("John Doe")
        env.asAdmin()
        env.dispatch(
            AuthAction.CreateUser(
                username = username,
                password = password,
                fullname = fullname,
                admin = false
            )
        )
        env.logout()
        assertDoesNotThrow {
            val token = env.dispatch(AuthAction.Login(username, password))
            env.env.verifyToken(token.accessToken, expectedSub = "john.doe")
        }
    }

    @Test
    fun `whoami called`() {
        val env = AuthActionEnvTest()
        val username = Username("john.doe")
        val password = PasswordClear("john.doe.0123456789")
        val fullname = Fullname("John Doe")
        env.asAdmin()
        env.dispatch(
            AuthAction.CreateUser(
                username = username,
                password = password,
                fullname = fullname,
                admin = false
            )
        )
        env.logout()
        assertThrows<AuthUnauthorizedException> {
            env.dispatch(AuthAction.WhoAmI())
        }

        env.logout()
        env.asAdmin()
        val whoamiAdmin = env.dispatch(AuthAction.WhoAmI())
        assertEquals(whoamiAdmin.issuer, env.env.oidcService.oidcIssuer())
        assertEquals(whoamiAdmin.admin, true)
        assertEquals(whoamiAdmin.sub, env.env.adminUsername.value)
        assertTrue(whoamiAdmin.roles.size == 1)
        assertEquals(ActorRole.ADMIN.key, whoamiAdmin.roles[0])

        env.asUser(username)
        val whoamiUser = env.dispatch(AuthAction.WhoAmI())
        assertEquals(whoamiUser.issuer, env.env.oidcService.oidcIssuer())
        assertEquals(whoamiUser.admin, false)
        assertEquals(whoamiUser.sub, username.value)
        assertTrue(whoamiUser.roles.isEmpty())

    }

    // TODO changeOwnPassword
    // TODO changeUserPassword
    // TODO disableUser
    // TODO changeFullname
    // TODO listActors
    // TODO setActorRoles
    // TODO disableActor
    // TODO enableActor
}