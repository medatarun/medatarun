package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.ActorPermission
import io.medatarun.auth.domain.AuthNotAuthenticatedException
import io.medatarun.auth.domain.UserDisableSelfException
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.ports.exposed.AuthJwtExternalPrincipal
import io.medatarun.lang.http.StatusCode
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.security.AppActorSystemMaintenance
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@EnableDatabaseTests
class AuthActionsTest {

    // ------------------------------------------------------------------------
    // Actions on our users and propagation to actors
    // ------------------------------------------------------------------------

    @Test
    fun `bootstrap called`() {
        val env = AuthEnvTest(createAdmin = false,)
        val username = Username("admin")
        val password = PasswordClear("admin.0123456789")
        // Important, keep the type to check response type
        val token: OAuthTokenResponseDto = env.dispatch(
            AuthAction.AdminBootstrap(
                secret = env.bootstrapSecretKeeper,
                username = username,
                password = password,
                fullname = Fullname("Admin")
            )
        )
        assertDoesNotThrow {
            env.verifyToken(token.accessToken, expectedSub = "admin")
        }

        val userService = env.userService
        val user = userService.loginUser(username, password)
        assertEquals(username, user.username)

        // Test user exists in Actors
        val actorService = env.actorService
        val actor = actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), user.username.value)
        assertNotNull(actor)

        val permissions = actorService.findActorPermissionSet(actor.id)

        assertEquals(1, permissions.size)
        assertTrue(permissions.any { it.key == ActorPermission.ADMIN.key })
    }

    @Test
    fun `create user called`() {
        val env = AuthEnvTest()
        val username = Username("john.doe")
        val password = PasswordClear("john.doe.0123456789")
        val fullname = Fullname("John Doe")
        env.asAdmin()
        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(
            AuthAction.UserCreate(
                username = username,
                password = password,
                fullname = fullname,
                admin = false
            )
        )
        val user = env.userService.loginUser(username, password)
        assertFalse(user.admin)
        assertEquals(username, user.username)
        assertEquals(fullname, user.fullname)
    }

    @Test
    fun `login user called`() {
        val env = AuthEnvTest()
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
            val token: OAuthTokenResponseDto = env.dispatch(AuthAction.Login(username, password))
            env.verifyToken(token.accessToken, expectedSub = "john.doe")
        }
    }

    @Test
    fun `whoami called`() {
        val env = AuthEnvTest()
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
        val error = assertThrows<ActionInvocationException> {
            env.dispatch(AuthAction.WhoAmI())
        }
        assertEquals(StatusCode.UNAUTHORIZED, error.status)

        env.logout()
        env.asAdmin()
        val whoamiAdmin: WhoAmIRespDto = env.dispatch(AuthAction.WhoAmI())
        assertEquals(whoamiAdmin.issuer, env.oidcService.oidcIssuer())
        assertEquals(whoamiAdmin.admin, true)
        assertEquals(whoamiAdmin.sub, env.adminUsername.value)
        assertTrue(whoamiAdmin.roles.size == 1)
        assertEquals(ActorPermission.ADMIN.key, whoamiAdmin.roles[0])

        env.asUser(username)
        val whoamiUser = env.dispatch(AuthAction.WhoAmI())
        assertEquals(whoamiUser.issuer, env.oidcService.oidcIssuer())
        assertEquals(whoamiUser.admin, false)
        assertEquals(whoamiUser.sub, username.value)
        assertTrue(whoamiUser.roles.isEmpty())

    }

    @Test
    fun `change own password called`() {
        val env = AuthEnvTest()
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
        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.ChangeMyPassword(password, passwordNext))
        // Allow testing that we didn't invert passwords
        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(AuthAction.Login(username, password))
        }
        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(username, passwordNext))
        }

    }

    @Test
    fun `change user password called`() {
        val env = AuthEnvTest()
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

        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.UserChangePassword(username, passwordNext))

        env.logout()
        // Allow testing that we didn't invert passwords
        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(AuthAction.Login(username, password))
        }
        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(username, passwordNext))
            // and that we didn't broke admin
            env.dispatch(AuthAction.Login(env.adminUsername, env.adminPassword))
        }
    }

    @Test
    fun `disable user called`() {
        val env = AuthEnvTest()
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

        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.UserDisable(username))

        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(AuthAction.Login(username, password))
        }

        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(env.adminUsername, env.adminPassword))
        }

        // Makes sure this propagates to actors
        val actorDisabled =
            env.actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), username.value)
        assertTrue(actorDisabled?.disabledDate != null)

    }

    @Test
    fun `enable user called`() {
        val env = AuthEnvTest()
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

        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.UserEnable(username))

        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(username, password))
        }

        // Makes sure this propagates to actors
        val actorEnabled =
            env.actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), username.value)
        assertNotNull(actorEnabled)
        assertEquals(null, actorEnabled.disabledDate)
    }

    @Test
    fun `actor disable uses user service for internal issuer`() {
        val env = AuthEnvTest()
        val username = Username("jane.doe")
        val password = PasswordClear("jane.doe.0123456789")
        val fullname = Fullname("Jane Doe")
        env.asAdmin()
        env.dispatch(
            AuthAction.UserCreate(
                username = username,
                password = password,
                fullname = fullname,
                admin = false
            )
        )

        val actor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            username.value
        )
        assertNotNull(actor)

        env.dispatch(AuthAction.ActorDisable(actor.id))

        env.logout()
        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(AuthAction.Login(username, password))
        }

        val actorDisabled = env.actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), username.value)
        assertTrue(actorDisabled?.disabledDate != null)

        env.asAdmin()
        env.dispatch(AuthAction.ActorEnable(actor.id))
        env.logout()

        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(username, password))
        }

        val actorEnabled =
            env.actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), username.value)
        assertNotNull(actorEnabled)
        assertEquals(null, actorEnabled.disabledDate)
    }

    @Test
    fun `actor disable uses actor service for external issuer`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val issuer = "https://example.com/oidc"
        val subject = "external.user"
        env.actorService.syncFromJwtExternalPrincipal(createActorJwt(issuer, subject))
        val actor = env.actorService.findByIssuerAndSubjectOptional(issuer, subject)
        assertNotNull(actor)

        env.dispatch(AuthAction.ActorDisable(actor.id))

        val actorDisabled = env.actorService.findByIssuerAndSubjectOptional(issuer, subject)
        assertNotNull(actorDisabled)
        assertTrue(actorDisabled.disabledDate != null)

        env.dispatch(AuthAction.ActorEnable(actor.id))

        val actorEnabled = env.actorService.findByIssuerAndSubjectOptional(issuer, subject)
        assertNotNull(actorEnabled)
        assertEquals(null, actorEnabled.disabledDate)
    }

    @Test
    fun `change user full name`() {
        val env = AuthEnvTest()
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

        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.UserChangeFullname(username, fullnameNext))

        env.asUser(username)

        // Test on our user database
        val user = env.userService.loginUser(username, password)
        assertEquals(fullnameNext, user.fullname)

        // We can test with whoami, which makes sure that it propagated to actors
        val whoami = env.dispatch(AuthAction.WhoAmI())
        assertEquals(fullnameNext.value, whoami.fullname)

    }

    @Test
    fun actorList() {
        val env = AuthEnvTest()
        env.asAdmin()
        env.dispatch(
            AuthAction.UserCreate(
                username = Username("john.doe"),
                password = PasswordClear("john.doe.0123456789"),
                fullname = Fullname("John Doe"),
                admin = false
            )
        )
        env.dispatch(
            AuthAction.UserCreate(
                username = Username("john.doe2"),
                password = PasswordClear("john.doe2.0123456789"),
                fullname = Fullname("John Doe2"),
                admin = false
            )
        )

        env.actorService.syncFromJwtExternalPrincipal(
            createActorJwt(
                "https://microsoft.com/azuread/123456789",
                "sandra.tafroilanuit",
                "Sandra Tafroilanuit",
                "sandra.tafroilanuit@test.azure.local"
            )
        )

        val actors: List<ActorInfoDto> = env.dispatch(AuthAction.ActorList())
        assertEquals(5, actors.size)

        val sysActor = actors.first { actor -> actor.id == AppActorSystemMaintenance.id.asString() }
        assertEquals(AppActorSystemMaintenance.SYSTEM_MAINTENANCE_SUBJECT, sysActor.subject)
        assertEquals(AppActorSystemMaintenance.SYSTEM_MAINTENANCE_ISSUER, sysActor.issuer)
        assertEquals(AppActorSystemMaintenance.displayName, sysActor.fullname)
        assertEquals(UuidUtils.getInstant(AppActorSystemMaintenance.SYSTEM_MAINTENANCE_ACTOR_ID), sysActor.createdAt)
        assertEquals(UuidUtils.getInstant(AppActorSystemMaintenance.SYSTEM_MAINTENANCE_ACTOR_ID), sysActor.lastSeenAt)

        val adminActor = actors.first { actor -> actor.subject == env.adminUsername.value }
        assertEquals(env.adminUsername.value, adminActor.subject)
        assertEquals(env.adminFullname.value, adminActor.fullname)
        assertEquals(env.oidcService.oidcIssuer(), adminActor.issuer)
        assertDoesNotThrow { UuidUtils.fromString(adminActor.id) }
        assertEquals(
            env.authClockTests.staticNow.truncatedTo(ChronoUnit.MILLIS),
            adminActor.createdAt.truncatedTo(ChronoUnit.MILLIS)
        )
        assertEquals(
            env.authClockTests.staticNow.truncatedTo(ChronoUnit.MILLIS),
            adminActor.lastSeenAt.truncatedTo(ChronoUnit.MILLIS)
        )

        val johnActor = actors.first { actor -> actor.subject == Username("john.doe").value }
        assertEquals("john.doe", johnActor.subject)
        assertEquals("John Doe", johnActor.fullname)
        assertEquals(env.oidcService.oidcIssuer(), johnActor.issuer)

        val john2Actor = actors.first { actor -> actor.subject == Username("john.doe2").value }
        assertEquals("john.doe2", john2Actor.subject)
        assertEquals("John Doe2", john2Actor.fullname)
        assertEquals(env.oidcService.oidcIssuer(), john2Actor.issuer)

        val sandraActor = actors.first { actor -> actor.subject == Username("sandra.tafroilanuit").value }
        assertEquals("sandra.tafroilanuit", sandraActor.subject)
        assertEquals("Sandra Tafroilanuit", sandraActor.fullname)
        assertEquals("https://microsoft.com/azuread/123456789", sandraActor.issuer)

    }

    @Test
    fun actorGet() {
        val env = AuthEnvTest()
        env.asAdmin()

        env.dispatch(
            AuthAction.UserCreate(
                username = Username("jane.doe"),
                password = PasswordClear("jane.doe.0123456789"),
                fullname = Fullname("Jane Doe"),
                admin = false
            )
        )

        val actor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            "jane.doe"
        )
        assertNotNull(actor)

        val actorInfo: ActorDetailDto = env.dispatch(AuthAction.ActorGet(actor.id))
        assertEquals(actor.id.value.toString(), actorInfo.id)
        assertEquals("jane.doe", actorInfo.subject)
        assertEquals("Jane Doe", actorInfo.fullname)
        assertEquals(env.oidcService.oidcIssuer(), actorInfo.issuer)
    }


    private fun createActorJwt(
        issuer: String,
        subject: String,
        name: String? = null,
        email: String? = null
    ): AuthJwtExternalPrincipal {
        return object : AuthJwtExternalPrincipal {
            override val issuer: String = issuer
            override val subject: String = subject
            override val issuedAt: Instant? = null
            override val expiresAt: Instant? = null
            override val audience: List<String> = emptyList()
            override val name: String? = name
            override val fullname: String? = null
            override val preferredUsername: String? = null
            override val email: String? = email
        }
    }

    // ------------------------------------------------------------------------
    // Actions on actors only
    // ------------------------------------------------------------------------

}
