package io.medatarun.lang.strings

import org.junit.jupiter.api.Assertions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TrimToNullTest {
    @Test
    fun `null is null`() {
        val str: String? = null
        Assertions.assertNull(str.trimToNull())
    }
    @Test
    fun `empty is null`() {
        val str: String? = ""
        Assertions.assertNull(str.trimToNull())
    }
    @Test
    fun `blank is null`() {
        val str: String = "   "
        Assertions.assertNull(str.trimToNull())
    }
    @Test
    fun `trimmed is not null`() {
        val str: String = "  a "
        assertNotNull(str.trimToNull())
        assertEquals("a", str.trimToNull())
    }
}