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
        assertTrue(actor.roles.any { it.key == ActorRole.ADMIN.key })
    }

    @Test
    fun `create user called`() {
        val env = AuthActionEnvTest()
        val username = Username("john.doe")
        val password = PasswordClear("john.doe.0123456789")
        val fullname = Fullname("John Doe")
        env.asAdmin()
        env.dispatch(
            AuthAction.UserCreate(
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
            AuthAction.UserCreate(
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
            AuthAction.UserCreate(
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

    @Test
    fun `change own password called`() {
        val env = AuthActionEnvTest()
        val username = Username("john.doe")
        val password = PasswordClear("john.doe.0123456789")
        val passwordNext = PasswordClear("john.doe.987654321")
        val fullname = Fullname("John Doe")
        env.asAdmin()
        env.dispatch(
            AuthAction.UserCreate(
                username = username,
                password = password,
                fullname = fullname,
                admin = false
            )
        )
        env.asUser(username)
        env.dispatch(AuthAction.ChangeMyPassword(password, passwordNext))
        // Allow testing that we didn't invert passwords
        assertThrows<AuthUnauthorizedException> {
            env.dispatch(AuthAction.Login(username, password))
        }
        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(username, passwordNext))
        }

    }

    @Test
    fun `change user password called`() {
        val env = AuthActionEnvTest()
        val username = Username("john.doe")
        val password = PasswordClear("john.doe.0123456789")
        val passwordNext = PasswordClear("john.doe.987654321")
        val fullname = Fullname("John Doe")
        env.asAdmin()
        env.dispatch(
            AuthAction.UserCreate(
                username = username,
                password = password,
                fullname = fullname,
                admin = false
            )
        )
        env.asAdmin()
        env.dispatch(AuthAction.UserChangePassword(username, passwordNext))

        env.logout()
        // Allow testing that we didn't invert passwords
        assertThrows<AuthUnauthorizedException> {
            env.dispatch(AuthAction.Login(username, password))
        }
        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(username, passwordNext))
            // and that we didn't broke admin
            env.dispatch(AuthAction.Login(env.env.adminUsername, env.env.adminPassword))
        }
    }

    @Test
    fun `disable user called`() {
        val env = AuthActionEnvTest()
        val username = Username("john.doe")
        val password = PasswordClear("john.doe.0123456789")
        val fullname = Fullname("John Doe")
        env.asAdmin()
        env.dispatch(
            AuthAction.UserCreate(
                username = username,
                password = password,
                fullname = fullname,
                admin = false
            )
        )

        env.dispatch(AuthAction.UserDisable(username))
        assertThrows<AuthUnauthorizedException> {
            env.dispatch(AuthAction.Login(username, password))
        }

        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(env.env.adminUsername, env.env.adminPassword))
        }

        // Makes sure this propagates to actors
        val actorDisabled = env.env.actorService.findByIssuerAndSubjectOptional(env.env.oidcService.oidcIssuer(), username.value)
        assertTrue(actorDisabled?.disabledDate != null)

    }

    @Test
    fun `change user full name`() {
        val env = AuthActionEnvTest()
        val username = Username("john.doe")
        val password = PasswordClear("john.doe.0123456789")
        val fullname = Fullname("John Doe")
        val fullnameNext = Fullname("New John Doe")
        env.asAdmin()
        env.dispatch(
            AuthAction.UserCreate(
                username = username,
                password = password,
                fullname = fullname,
                admin = false
            )
        )

        env.dispatch(AuthAction.UserChangeFullname(username, fullnameNext))
        env.asUser(username)

        // Test on our user database
        val user = env.env.userService.loginUser(username, password)
        assertEquals(fullnameNext, user.fullname)

        // We can test with whoami, which makes sure that it propagated to actors
        val whoami = env.dispatch(AuthAction.WhoAmI())
        assertEquals(fullnameNext.value, whoami.fullname)

    }


    // TODO listActors
    // TODO setActorRoles
    // TODO disableActor
    // TODO enableActor
}