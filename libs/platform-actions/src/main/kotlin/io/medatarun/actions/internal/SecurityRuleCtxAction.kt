package io.medatarun.actions.internal

import io.medatarun.actions.ports.needs.ActionRequestCtx
import io.medatarun.security.AppPermission
import io.medatarun.security.SecurityRuleCtx
import io.medatarun.security.SecurityRuleEvaluatorResult

internal class SecurityRuleCtxAction(private val actionCtx: ActionRequestCtx) : SecurityRuleCtx {

    override fun isSignedIn(): Boolean {
        return actionCtx.principalCtx.principal != null
    }

    override fun isAdmin(): Boolean {
        val p = actionCtx.principalCtx.principal
        return p != null && p.isAdmin
    }

    override fun getPermissions(): List<AppPermission> {
        return actionCtx.principalCtx.principal?.permissions ?: emptyList()
    }

    override fun ensurePermission(wantedPermission: AppPermission): SecurityRuleEvaluatorResult {
        val ok = getPermissions().any { it.key == wantedPermission.key }
        return if (ok)
            SecurityRuleEvaluatorResult.Ok()
        else
            SecurityRuleEvaluatorResult.AuthorizationError("Not authorized. Permission [${wantedPermission.key}] is needed.")
    }
}