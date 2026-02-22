package io.medatarun.tags.core.domain

import io.medatarun.type.commons.key.KeyStrictInvalidFormatException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TagGroupKeyTest {
    @Test
    fun `validated accepts hashtag like group key`() {
        val key = TagGroupKey("Project_ALPHA-1")

        val result = key.validated()

        assertEquals(key, result)
    }

    @Test
    fun `validated rejects slash`() {
        assertFailsWith<KeyStrictInvalidFormatException> {
            TagGroupKey("Domain/Core").validated()
        }
    }
}
