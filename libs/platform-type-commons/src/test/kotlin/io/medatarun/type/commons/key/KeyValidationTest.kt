package io.medatarun.type.commons.key

import org.junit.jupiter.api.Assertions
import kotlin.test.Test

class KeyValidationTest {
    @Test
    fun `valid key is accepted`() {
        val key = "cus.customer-01.\$defs"
        val result = KeyValidation.validate(key)
        Assertions.assertEquals(key, result)
    }

    @Test
    fun `empty key is rejected`() {
        Assertions.assertThrows(KeyEmptyException::class.java) {
            KeyValidation.validate("")
        }
    }

    @Test
    fun `key longer than max length is rejected`() {
        val key = "a".repeat(KeyValidation.MAX_LENGTH + 1)
        Assertions.assertThrows(KeyTooLongException::class.java) {
            KeyValidation.validate(key)
        }
    }

    @Test
    fun `control characters are rejected`() {
        val key = "abc\u0007def" // bell
        Assertions.assertThrows(KeyInvalidFormatException::class.java) {
            KeyValidation.validate(key)
        }
    }

    @Test
    fun `non printable ascii characters are rejected`() {
        val key = "abc\u0080def"
        Assertions.assertThrows(KeyInvalidFormatException::class.java) {
            KeyValidation.validate(key)
        }
    }

    @Test
    fun `backslash is rejected`() {
        Assertions.assertThrows(KeyInvalidFormatException::class.java) {
            KeyValidation.validate("abc\\def")
        }
    }

    @Test
    fun `slash is accepted`() {
        val key = "abc/def"
        val result = KeyValidation.validate(key)
        Assertions.assertEquals(key, result)
    }

    @Test
    fun `quotes are rejected`() {
        Assertions.assertThrows(KeyInvalidFormatException::class.java) {
            KeyValidation.validate("abc\"def")
        }
        Assertions.assertThrows(KeyInvalidFormatException::class.java) {
            KeyValidation.validate("abc'def")
        }
    }

    @Test
    fun `backtick is rejected`() {
        Assertions.assertThrows(KeyInvalidFormatException::class.java) {
            KeyValidation.validate("abc`def")
        }
    }

}
