package io.medatarun.auth.actions

import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.AuthUnauthorizedException
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthActionEnvTest
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.AuthJwtExternalPrincipal
import io.medatarun.auth.ports.exposed.UserService
import io.medatarun.lang.uuid.UuidUtils
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class AuthActionsTest {

    // ------------------------------------------------------------------------
    // Actions on our users and propagation to actors
    // ------------------------------------------------------------------------

    @Test
    fun `bootstrap called`() {
        val env = AuthActionEnvTest(createAdmin = false)
        val username = Username("admin")
        val password = PasswordClear("admin.0123456789")
        // Important, keep the type to check response type
        val token: OAuthTokenResponseDto = env.dispatch(
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
        assertEquals(username, user.username)

        // Test user exists in Actors
        val actorService = env.getService(ActorService::class)
        val actor = actorService.findByIssuerAndSubjectOptional(env.env.oidcService.oidcIssuer(), user.username.value)
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
        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(
            AuthAction.UserCreate(
                username = username,
                password = password,
                fullname = fullname,
                admin = false
            )
        )
        val user = env.getService(UserService::class).loginUser(username, password)
        assertFalse(user.admin)
        assertEquals(username, user.username)
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
            val token: OAuthTokenResponseDto = env.dispatch(AuthAction.Login(username, password))
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
        val whoamiAdmin: WhoAmIRespDto = env.dispatch(AuthAction.WhoAmI())
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
        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.ChangeMyPassword(password, passwordNext))
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

        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.UserChangePassword(username, passwordNext))

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

        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.UserDisable(username))

        assertThrows<AuthUnauthorizedException> {
            env.dispatch(AuthAction.Login(username, password))
        }

        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(env.env.adminUsername, env.env.adminPassword))
        }

        // Makes sure this propagates to actors
        val actorDisabled =
            env.env.actorService.findByIssuerAndSubjectOptional(env.env.oidcService.oidcIssuer(), username.value)
        assertTrue(actorDisabled?.disabledDate != null)

    }

    @Test
    fun `enable user called`() {
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

        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.UserEnable(username))

        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(username, password))
        }

        // Makes sure this propagates to actors
        val actorEnabled =
            env.env.actorService.findByIssuerAndSubjectOptional(env.env.oidcService.oidcIssuer(), username.value)
        assertNotNull(actorEnabled)
        assertEquals(null, actorEnabled.disabledDate)
    }

    @Test
    fun `actor disable uses user service for internal issuer`() {
        val env = AuthActionEnvTest()
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

        val actor = env.env.actorService.findByIssuerAndSubjectOptional(
            env.env.oidcService.oidcIssuer(),
            username.value
        )
        assertNotNull(actor)

        env.dispatch(AuthAction.ActorDisable(actor.id))

        env.logout()
        assertThrows<AuthUnauthorizedException> {
            env.dispatch(AuthAction.Login(username, password))
        }

        val actorDisabled =
            env.env.actorService.findByIssuerAndSubjectOptional(env.env.oidcService.oidcIssuer(), username.value)
        assertTrue(actorDisabled?.disabledDate != null)

        env.dispatch(AuthAction.ActorEnable(actor.id))

        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(username, password))
        }

        val actorEnabled =
            env.env.actorService.findByIssuerAndSubjectOptional(env.env.oidcService.oidcIssuer(), username.value)
        assertNotNull(actorEnabled)
        assertEquals(null, actorEnabled.disabledDate)
    }

    @Test
    fun `actor disable uses actor service for external issuer`() {
        val env = AuthActionEnvTest()
        env.asAdmin()
        val issuer = "https://example.com/oidc"
        val subject = "external.user"
        env.env.actorService.syncFromJwtExternalPrincipal(createActorJwt(issuer, subject))
        val actor = env.env.actorService.findByIssuerAndSubjectOptional(issuer, subject)
        assertNotNull(actor)

        env.dispatch(AuthAction.ActorDisable(actor.id))

        val actorDisabled = env.env.actorService.findByIssuerAndSubjectOptional(issuer, subject)
        assertNotNull(actorDisabled)
        assertTrue(actorDisabled.disabledDate != null)

        env.dispatch(AuthAction.ActorEnable(actor.id))

        val actorEnabled = env.env.actorService.findByIssuerAndSubjectOptional(issuer, subject)
        assertNotNull(actorEnabled)
        assertEquals(null, actorEnabled.disabledDate)
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

        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.UserChangeFullname(username, fullnameNext))

        env.asUser(username)

        // Test on our user database
        val user = env.env.userService.loginUser(username, password)
        assertEquals(fullnameNext, user.fullname)

        // We can test with whoami, which makes sure that it propagated to actors
        val whoami = env.dispatch(AuthAction.WhoAmI())
        assertEquals(fullnameNext.value, whoami.fullname)

    }

    @Test
    fun actorList() {
        val env = AuthActionEnvTest()
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

        env.env.actorService.syncFromJwtExternalPrincipal(
            createActorJwt(
                "https://microsoft.com/azuread/123456789",
                "sandra.tafroilanuit",
                "Sandra Tafroilanuit",
                "sandra.tafroilanuit@test.azure.local"
            )
        )

        val actors: List<ActorInfoDto> = env.dispatch(AuthAction.ActorList())
        assertEquals(4, actors.size)

        val adminActor = actors.first { actor -> actor.subject == env.env.adminUsername.value }
        assertEquals(env.env.adminUsername.value, adminActor.subject)
        assertEquals(env.env.adminFullname.value, adminActor.fullname)
        assertEquals(env.env.oidcService.oidcIssuer(), adminActor.issuer)
        assertDoesNotThrow { UuidUtils.fromString(adminActor.id) }
        assertEquals(env.env.authClock.staticNow, adminActor.createdAt)
        assertEquals(env.env.authClock.staticNow, adminActor.lastSeenAt)

        val johnActor = actors.first { actor -> actor.subject == Username("john.doe").value }
        assertEquals("john.doe", johnActor.subject)
        assertEquals("John Doe", johnActor.fullname)
        assertEquals(env.env.oidcService.oidcIssuer(), johnActor.issuer)

        val john2Actor = actors.first { actor -> actor.subject == Username("john.doe2").value }
        assertEquals("john.doe2", john2Actor.subject)
        assertEquals("John Doe2", john2Actor.fullname)
        assertEquals(env.env.oidcService.oidcIssuer(), john2Actor.issuer)

        val sandraActor = actors.first { actor -> actor.subject == Username("sandra.tafroilanuit").value }
        assertEquals("sandra.tafroilanuit", sandraActor.subject)
        assertEquals("Sandra Tafroilanuit", sandraActor.fullname)
        assertEquals("https://microsoft.com/azuread/123456789", sandraActor.issuer)

    }

    @Test
    fun actorGet() {
        val env = AuthActionEnvTest()
        env.asAdmin()

        env.dispatch(
            AuthAction.UserCreate(
                username = Username("jane.doe"),
                password = PasswordClear("jane.doe.0123456789"),
                fullname = Fullname("Jane Doe"),
                admin = false
            )
        )

        val actor = env.env.actorService.findByIssuerAndSubjectOptional(
            env.env.oidcService.oidcIssuer(),
            "jane.doe"
        )
        assertNotNull(actor)

        val actorInfo: ActorInfoDto = env.dispatch(AuthAction.ActorGet(actor.id))
        assertEquals(actor.id.value.toString(), actorInfo.id)
        assertEquals("jane.doe", actorInfo.subject)
        assertEquals("Jane Doe", actorInfo.fullname)
        assertEquals(env.env.oidcService.oidcIssuer(), actorInfo.issuer)
    }

    @Test
    fun changeRolesOnActor() {
        val env = AuthActionEnvTest(otherRoles = setOf("ROLE1", "ROLE2"))
        env.asAdmin()
        val iss = "https://microsoft.com/azuread/987654321"
        val sub = "john.doe"
        env.env.actorService.syncFromJwtExternalPrincipal(createActorJwt(iss, sub))
        val actor = env.env.actorService.findByIssuerAndSubjectOptional(iss, sub)
        assertNotNull(actor)
        assertTrue(actor.roles.isEmpty())

        @Suppress("UnusedVariable", "unused") val result: Unit = env.dispatch(
            AuthAction.ActorSetRoles(actor.id, roles = listOf("ROLE1", "ROLE2"))
        )

        val actorAfter = env.env.actorService.findByIssuerAndSubjectOptional(iss, sub)
        assertNotNull(actorAfter)
        assertTrue(actorAfter.roles.any {it.key == "ROLE1" })
        assertTrue(actorAfter.roles.any {it.key == "ROLE2" })

        // Test empty
        env.dispatch(AuthAction.ActorSetRoles(actor.id, roles = emptyList()))
        val actorEmptyRoles = env.env.actorService.findByIssuerAndSubjectOptional(iss, sub)
        assertNotNull(actorEmptyRoles)
        assertTrue(actorEmptyRoles.roles.isEmpty())
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
            override val roles: List<String> = emptyList()
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
