package io.medatarun.type.commons.instant

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import java.time.Instant
import java.sql.Timestamp

class InstantAdaptersTest {
    @Test
    fun `toSqlTimestampString formats instant as sqlite jdbc compatible timestamp text`() {
        val instant = Instant.parse("2026-03-30T09:33:54.555163Z")

        val actual = InstantAdapters.toSqlTimestampString(instant)

        assertFalse(actual.contains("T"))
        assertFalse(actual.contains("Z"))
        assertNotNull(Timestamp.valueOf(actual))
    }
}
