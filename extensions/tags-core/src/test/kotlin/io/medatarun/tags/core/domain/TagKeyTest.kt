package io.medatarun.tags.core.domain

import io.medatarun.type.commons.key.KeyStrictInvalidFormatException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TagKeyTest {
    @Test
    fun `validated accepts hashtag like key`() {
        val key = TagKey("Release_2026-Q1")

        val result = key.validated()

        assertEquals(key, result)
    }

    @Test
    fun `validated rejects slash`() {
        assertFailsWith<KeyStrictInvalidFormatException> {
            TagKey("Team/Backend").validated()
        }
    }
}
