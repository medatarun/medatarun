package io.medatarun.type.commons.key

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KeyStrictValidationTest {
    @Test
    fun `strict key accepts letters digits underscore hyphen and uppercase`() {
        val key = "Release_2026-Q1"

        val result = KeyStrictValidation.validate(key)

        assertEquals(key, result)
    }

    @Test
    fun `strict key rejects slash`() {
        assertFailsWith<KeyStrictInvalidFormatException> {
            KeyStrictValidation.validate("abc/def")
        }
    }

    @Test
    fun `strict key rejects space`() {
        assertFailsWith<KeyStrictInvalidFormatException> {
            KeyStrictValidation.validate("abc def")
        }
    }

    @Test
    fun `strict key rejects empty`() {
        assertFailsWith<KeyStrictEmptyException> {
            KeyStrictValidation.validate("")
        }
    }

    @Test
    fun `strict key rejects values longer than max length`() {
        assertFailsWith<KeyStrictTooLongException> {
            KeyStrictValidation.validate("a".repeat(KeyStrictValidation.MAX_LENGTH + 1))
        }
    }

    @Test
    fun `strict key rejects dot`() {
        assertFailsWith<KeyStrictInvalidFormatException> {
            KeyStrictValidation.validate("abc.def")
        }
    }

    @Test
    fun `strict key rejects backslash`() {
        assertFailsWith<KeyStrictInvalidFormatException> {
            KeyStrictValidation.validate("abc\\def")
        }
    }
}
