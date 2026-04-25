package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.ActorPermission
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.lang.http.StatusCode
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@EnableDatabaseTests
class WhoAmI_Test {
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
        assertTrue(whoamiAdmin.permissions.size == 1)
        assertEquals(ActorPermission.ADMIN.key, whoamiAdmin.permissions[0])

        env.asUser(username)
        val whoamiUser = env.dispatch(AuthAction.WhoAmI())
        assertEquals(whoamiUser.issuer, env.oidcService.oidcIssuer())
        assertEquals(whoamiUser.admin, false)
        assertEquals(whoamiUser.sub, username.value)
        assertTrue(whoamiUser.permissions.isEmpty())

    }

}