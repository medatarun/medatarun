package io.medatarun.auth.domain

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class UsernameTest {
    @Test
    fun `valid usernames are accepted`() {
        val valid = listOf(
            "jean",
            "jean_dupont",
            "jean-dupont",
            "jean.d",
            "user123",
            "john-doe42"
        )

        valid.forEach { name ->
            assertDoesNotThrow(name) { Username(name).validate() }
        }
    }

    @Test
    fun `empty username is rejected`() {
        assertThrows<UsernameEmptyException> {
            Username("").validate()
        }
    }

    @Test
    fun `too short username is rejected`() {
        assertThrows<UsernameTooShortException> {
            Username("ab").validate()
        }
    }

    @Test
    fun `invalid usernames are rejected`() {
        val invalid = listOf(
            "Jean",          // majuscule
            "jean dupont",   // espace
            "_jean",         // commence par séparateur
            "jean_",         // finit par séparateur
            "jean--dupont",  // séparateurs consécutifs
            "jean@dupont",   // symbole
            "jéan",          // accent
            "😀user"         // emoji
        )

        invalid.forEach {
            assertThrows<UsernameInvalidFormatException> {
                Username(it).validate()
            }
        }
    }
}