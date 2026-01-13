package io.medatarun.model.internal

import io.medatarun.model.domain.KeyEmptyException
import io.medatarun.model.domain.KeyInvalidFormatException
import io.medatarun.model.domain.KeyTooLongException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test

class KeyValidationTest {
    @Test
    fun `valid key is accepted`() {
        val key = "cus.customer-01.\$defs"
        val result = KeyValidation.validate(key)
        assertEquals(key, result)
    }

    @Test
    fun `empty key is rejected`() {
        assertThrows(KeyEmptyException::class.java) {
            KeyValidation.validate("")
        }
    }

    @Test
    fun `key longer than max length is rejected`() {
        val key = "a".repeat(KeyValidation.MAX_LENGTH + 1)
        assertThrows(KeyTooLongException::class.java) {
            KeyValidation.validate(key)
        }
    }

    @Test
    fun `control characters are rejected`() {
        val key = "abc\u0007def" // bell
        assertThrows(KeyInvalidFormatException::class.java) {
            KeyValidation.validate(key)
        }
    }

    @Test
    fun `non printable ascii characters are rejected`() {
        val key = "abc\u0080def"
        assertThrows(KeyInvalidFormatException::class.java) {
            KeyValidation.validate(key)
        }
    }

    @Test
    fun `backslash is rejected`() {
        assertThrows(KeyInvalidFormatException::class.java) {
            KeyValidation.validate("abc\\def")
        }
    }

    @Test
    fun `quotes are rejected`() {
        assertThrows(KeyInvalidFormatException::class.java) {
            KeyValidation.validate("abc\"def")
        }
        assertThrows(KeyInvalidFormatException::class.java) {
            KeyValidation.validate("abc'def")
        }
    }

    @Test
    fun `backtick is rejected`() {
        assertThrows(KeyInvalidFormatException::class.java) {
            KeyValidation.validate("abc`def")
        }
    }
}