package io.medatarun.auth.actions

import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@EnableDatabaseTests
class Login_Test {

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

}