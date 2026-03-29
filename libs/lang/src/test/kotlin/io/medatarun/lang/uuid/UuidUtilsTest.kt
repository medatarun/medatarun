package io.medatarun.lang.uuid

import kotlin.test.Test
import kotlin.test.assertEquals

class UuidUtilsTest {
    @Test
    fun `getInstant reads v7 uuid timestamp`() {
        val uuid = UuidUtils.fromString("01941f29-7c00-7000-9a65-67088ebcbabd")
        val instant = UuidUtils.getInstant(uuid)
        assertEquals("2025-01-01T00:00:00Z", instant.toString())
    }
}
