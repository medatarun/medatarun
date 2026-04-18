package io.medatarun.auth.actions

import io.medatarun.auth.domain.UserAlreadyExistsException
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@EnableDatabaseTests
class UserCreate_Test {

    @Test
    fun `create user called`() {
        val env = AuthEnvTest()
        env.createJohn()

        // Assertions to test that user can sign in directly (without actions)
        val user = env.userService.findByUsername(env.johnUsername)
        assertFalse(user.admin)
        assertEquals(env.johnUsername, user.username)
        assertEquals(env.johnFullname, user.fullname)

    }

    @Test
    fun `can not create user with same login`() {
        val env = AuthEnvTest()

        env.asAdmin()
        env.dispatch(
            AuthAction.UserCreate(
                username = env.johnUsername,
                password = env.johnPassword,
                fullname = env.johnFullname,
                admin = false
            )
        )

        assertThrows<UserAlreadyExistsException> {
            env.dispatch(
                AuthAction.UserCreate(
                    username = env.johnUsername,
                    fullname = Fullname("Other"),
                    password = PasswordClear("other.name." + UuidUtils.generateV4String()), admin = false
                )
            )
        }
    }
    @Test
    fun `can not create user with same login of a disabled user`() {
        val env = AuthEnvTest()

        env.asAdmin()
        env.dispatch(
            AuthAction.UserCreate(
                username = env.johnUsername,
                password = env.johnPassword,
                fullname = env.johnFullname,
                admin = false
            )
        )

        env.dispatch(AuthAction.UserDisable(env.johnUsername))

        assertThrows<UserAlreadyExistsException> {
            env.dispatch(
                AuthAction.UserCreate(
                    username = env.johnUsername,
                    fullname = Fullname("Other"),
                    password = PasswordClear("other.name." + UuidUtils.generateV4String()), admin = false
                )
            )
        }
    }
}