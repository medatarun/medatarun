package io.medatarun.actions.internal

import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequestCtx
import io.medatarun.security.AppActorId
import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionKey
import io.medatarun.security.AppPrincipal
import org.junit.jupiter.api.Assertions
import kotlin.test.Test

class SecurityRuleCtxActionTest {

    @Test
    fun `principal present means signed in`() {
        val principal = TestPrincipal(isAdmin = false, permissions = emptySet())
        val ctx = SecurityRuleCtxAction(TestActionCtx(principal))

        Assertions.assertTrue(ctx.isSignedIn())
        Assertions.assertFalse(ctx.isAdmin())
        Assertions.assertEquals(emptySet<AppPermission>(), ctx.getPermissions())
    }

    @Test
    fun `admin principal is admin`() {
        val permission = AppPermissionKey("admin")
        val principal = TestPrincipal(isAdmin = true, permissions = setOf(permission))
        val ctx = SecurityRuleCtxAction(TestActionCtx(principal))

        Assertions.assertTrue(ctx.isSignedIn())
        Assertions.assertTrue(ctx.isAdmin())
        Assertions.assertEquals(setOf(permission), ctx.getPermissions())
    }

    @Test
    fun `missing principal means signed out`() {
        val ctx = SecurityRuleCtxAction(TestActionCtx(null))

        Assertions.assertFalse(ctx.isSignedIn())
        Assertions.assertFalse(ctx.isAdmin())
        Assertions.assertEquals(emptySet<AppPermission>(), ctx.getPermissions())
    }

    // fixtures minimales

    private class TestPrincipal(
        override val isAdmin: Boolean,
        override val permissions: Set<AppPermissionKey>
    ) : AppPrincipal {
        override val id = AppActorId.generate()
        override val issuer = "issuer"
        override val subject = "subject"
        override val fullname = "name"
    }



    private class TestActionCtx(principal: AppPrincipal?) : ActionRequestCtx {
        override val principalCtx = object : ActionPrincipalCtx {
            override val principal = principal
            override fun ensureIsAdmin() = error("not used")
            override fun ensureSignedIn(): AppPrincipal = error("not used")
        }
        override val source: String = "test"

    }
}
