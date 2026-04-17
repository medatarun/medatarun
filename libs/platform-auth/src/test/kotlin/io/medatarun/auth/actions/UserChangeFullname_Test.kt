package io.medatarun.auth.actions

import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@EnableDatabaseTests
class UserChangeFullname_Test {
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

}