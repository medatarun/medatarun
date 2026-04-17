package io.medatarun.auth.actions

import io.medatarun.auth.domain.ActorPermission
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@EnableDatabaseTests
class AdminBootstrap_Test {

    @Test
    fun `bootstrap called`() {
        val env = AuthEnvTest(createAdmin = false)
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
}