package io.medatarun.actions.adapters

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.security.AppPrincipal
import io.medatarun.security.AppPrincipalId
import io.medatarun.security.AppPrincipalRole
import org.junit.jupiter.api.Assertions
import kotlin.reflect.KClass
import kotlin.test.Test

class SecurityRuleCtxActionTest {

    @Test
    fun `principal present means signed in`() {
        val principal = TestPrincipal(isAdmin = false, roles = emptyList())
        val ctx = SecurityRuleCtxAction(TestActionCtx(principal))

        Assertions.assertTrue(ctx.isSignedIn())
        Assertions.assertFalse(ctx.isAdmin())
        Assertions.assertEquals(emptyList<AppPrincipalRole>(), ctx.getRoles())
    }

    @Test
    fun `admin principal is admin`() {
        val role = TestRole("admin")
        val principal = TestPrincipal(isAdmin = true, roles = listOf(role))
        val ctx = SecurityRuleCtxAction(TestActionCtx(principal))

        Assertions.assertTrue(ctx.isSignedIn())
        Assertions.assertTrue(ctx.isAdmin())
        Assertions.assertEquals(listOf(role), ctx.getRoles())
    }

    @Test
    fun `missing principal means signed out`() {
        val ctx = SecurityRuleCtxAction(TestActionCtx(null))

        Assertions.assertFalse(ctx.isSignedIn())
        Assertions.assertFalse(ctx.isAdmin())
        Assertions.assertEquals(emptyList<AppPrincipalRole>(), ctx.getRoles())
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