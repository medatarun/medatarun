package io.medatarun.actions.infra.db

import io.medatarun.actions.domain.ActionInvocationException
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ActionAuditRecorderDbTest {
    @Test
    fun `successful action is stored in database`() {
        val env = ActionAuditDbTestEnv()

        env.dispatch(ActionAuditDbTestEnv.TestDbAction.Ok)

        val row = env.auditRows().single()
        assertEquals("test-db-audit", row.actionGroupKey)
        assertEquals("business-ok", row.actionKey)
        assertEquals("test", row.source)
        assertEquals(env.actionAuditClockTests.staticNow, row.createdAt)
        assertEquals("SUCCEEDED", row.status)
        assertNotNull(row.actionInstanceId)
        assertEquals(null, row.errorCode)
        assertEquals(null, row.errorMessage)
    }

    @Test
    fun `rejected action is stored in database`() {
        val env = ActionAuditDbTestEnv()

        assertThrows<ActionInvocationException> {
            env.dispatch(ActionAuditDbTestEnv.TestDbAction.Denied)
        }

        val row = env.auditRows().single()
        assertEquals("security-denied", row.actionKey)
        assertEquals("REJECTED", row.status)
        assertEquals("FORBIDDEN", row.errorCode)
        assertEquals("Unauthorized", row.errorMessage)
    }

    @Test
    fun `failed action is stored in database`() {
        val env = ActionAuditDbTestEnv()

        val ex = assertThrows<ActionAuditDbTestEnv.TestActionBusinessFailureException> {
            env.dispatch(ActionAuditDbTestEnv.TestDbAction.BusinessFails)
        }

        val row = env.auditRows().single()
        assertEquals("boom", ex.message)
        assertEquals("business-fails", row.actionKey)
        assertEquals("FAILED", row.status)
        assertEquals("TestActionBusinessFailureException", row.errorCode)
        assertEquals("boom", row.errorMessage)
    }
}
