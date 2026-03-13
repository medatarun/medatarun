package io.medatarun.actions.internal

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.security.AppPrincipalRole
import io.medatarun.security.SecurityRuleCtx
import io.medatarun.security.SecurityRuleEvaluatorResult

internal class SecurityRuleCtxAction(private val actionCtx: ActionCtx) : SecurityRuleCtx {

    override fun isSignedIn(): Boolean {
        return actionCtx.principal.principal != null
    }

    override fun isAdmin(): Boolean {
        val p = actionCtx.principal.principal
        return p != null && p.isAdmin
    }

    override fun getRoles(): List<AppPrincipalRole> {
        return actionCtx.principal.principal?.roles ?: emptyList()
    }

    override fun ensureRole(wantedRole: AppPrincipalRole): SecurityRuleEvaluatorResult {

            val ok = getRoles().any { it.key == wantedRole.key }
            return if (ok) SecurityRuleEvaluatorResult.Ok() else SecurityRuleEvaluatorResult.Error("Not authorized. Role [${wantedRole.key}] is needed.")

    }
}