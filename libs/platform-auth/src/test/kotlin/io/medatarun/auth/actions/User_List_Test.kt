package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.lang.http.StatusCode
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@EnableDatabaseTests
class User_List_Test {

    /**
     * Verifies that the user list exposes each account's business identity, admin marker,
     * and enabled or disabled status.
     */
    @Test
    fun `list users with their status and admin marker`() {
        val env = AuthEnvTest()
        env.asAdmin()
        env.dispatch(
            AuthAction.User_Create(
                username = env.johnUsername,
                fullname = env.johnFullname,
                password = env.johnPassword,
                admin = false
            )
        )
        val otherAdminUsername = Username("other.admin")
        val otherAdminFullname = Fullname("Other Admin")
        env.dispatch(
            AuthAction.User_Create(
                username = otherAdminUsername,
                fullname = otherAdminFullname,
                password = PasswordClear("other.admin." + UuidUtils.generateV4String()),
                admin = true
            )
        )
        val disabledAt = env.authClockTests.now().plusSeconds(60)
        env.authClockTests.staticNow = disabledAt
        env.dispatch(AuthAction.User_Disable(env.johnUsername))

        val users = env.dispatch(AuthAction.User_List()).items

        val listedUsers = users.associateBy { user -> user.username }
        val adminUser = env.userService.findByUsername(env.adminUsername)
        val johnUser = env.userService.findByUsername(env.johnUsername)
        val otherAdminUser = env.userService.findByUsername(otherAdminUsername)

        val listedAdmin = assertNotNull(listedUsers[env.adminUsername.value])
        assertEquals(adminUser.id.asString(), listedAdmin.id)
        assertEquals(env.adminUsername.value, listedAdmin.username)
        assertEquals(env.adminFullname.value, listedAdmin.fullname)
        assertEquals(true, listedAdmin.admin)
        assertEquals(null, listedAdmin.disabledDate)

        val listedJohn = assertNotNull(listedUsers[env.johnUsername.value])
        assertEquals(johnUser.id.asString(), listedJohn.id)
        assertEquals(env.johnUsername.value, listedJohn.username)
        assertEquals(env.johnFullname.value, listedJohn.fullname)
        assertEquals(false, listedJohn.admin)
        assertEquals(disabledAt, listedJohn.disabledDate)

        val listedOtherAdmin = assertNotNull(listedUsers[otherAdminUsername.value])
        assertEquals(otherAdminUser.id.asString(), listedOtherAdmin.id)
        assertEquals(otherAdminUsername.value, listedOtherAdmin.username)
        assertEquals(otherAdminFullname.value, listedOtherAdmin.fullname)
        assertEquals(true, listedOtherAdmin.admin)
        assertEquals(null, listedOtherAdmin.disabledDate)
    }

    /**
     * Verifies that users without administrator privileges cannot list user accounts.
     */
    @Test
    fun `refuse to list users when user is not admin`() {
        val env = AuthEnvTest()
        env.asAdmin()
        env.dispatch(
            AuthAction.User_Create(
                username = env.johnUsername,
                fullname = env.johnFullname,
                password = env.johnPassword,
                admin = false
            )
        )
        env.asUser(env.johnUsername)

        val error = assertThrows<ActionInvocationException> {
            env.dispatch(AuthAction.User_List())
        }

        assertEquals(StatusCode.FORBIDDEN, error.status)
    }
}
