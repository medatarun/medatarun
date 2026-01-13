package io.medatarun.actions.runtime

import io.medatarun.actions.adapters.SecurityRuleCtxAction
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.security.AppPrincipal
import io.medatarun.security.AppPrincipalId
import io.medatarun.security.AppPrincipalRole
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class ActionSecurityContextTest {

    @Test
    fun `principal present means signed in`() {
        val principal = TestPrincipal(isAdmin = false, roles = emptyList())
        val ctx = SecurityRuleCtxAction(TestActionCtx(principal))

        assertTrue(ctx.isSignedIn())
        assertFalse(ctx.isAdmin())
        assertEquals(emptyList<AppPrincipalRole>(), ctx.getRoles())
    }

    @Test
    fun `admin principal is admin`() {
        val role = TestRole("admin")
        val principal = TestPrincipal(isAdmin = true, roles = listOf(role))
        val ctx = SecurityRuleCtxAction(TestActionCtx(principal))

        assertTrue(ctx.isSignedIn())
        assertTrue(ctx.isAdmin())
        assertEquals(listOf(role), ctx.getRoles())
    }

    @Test
    fun `missing principal means signed out`() {
        val ctx = SecurityRuleCtxAction(TestActionCtx(null))

        assertFalse(ctx.isSignedIn())
        assertFalse(ctx.isAdmin())
        assertEquals(emptyList<AppPrincipalRole>(), ctx.getRoles())
    }

    // fixtures minimales

    private class TestPrincipal(
        override val isAdmin: Boolean,
        override val roles: List<AppPrincipalRole>
    ) : AppPrincipal {
        override val id = AppPrincipalId("id")
        override val issuer = "issuer"
        override val subject = "subject"
        override val fullname = "name"
    }

    private class TestRole(override val key: String) : AppPrincipalRole

    private class TestActionCtx(principal: AppPrincipal?) : ActionCtx {
        override val principal = object : ActionPrincipalCtx {
            override val principal = principal
            override fun ensureIsAdmin() = error("not used")
            override fun ensureSignedIn(): AppPrincipal = error("not used")
        }

        override val extensionRegistry get() = error("not used")
        override fun dispatchAction(req: ActionRequest): Any? = error("not used")
        override fun <T : Any> getService(type: KClass<T>): T = error("not used")
    }
}
