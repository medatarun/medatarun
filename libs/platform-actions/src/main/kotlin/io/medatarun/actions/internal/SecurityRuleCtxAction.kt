package io.medatarun.actions.internal

import io.medatarun.actions.ports.needs.ActionRequestCtx
import io.medatarun.security.AppPrincipalRole
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

    override fun getRoles(): List<AppPrincipalRole> {
        return actionCtx.principalCtx.principal?.roles ?: emptyList()
    }

    override fun ensureRole(wantedRole: AppPrincipalRole): SecurityRuleEvaluatorResult {

            val ok = getRoles().any { it.key == wantedRole.key }
            return if (ok) SecurityRuleEvaluatorResult.Ok() else SecurityRuleEvaluatorResult.AuthorizationError("Not authorized. Role [${wantedRole.key}] is needed.")

    }
}