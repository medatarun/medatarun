package io.medatarun.actions.adapters

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.security.AppPrincipalRole
import io.medatarun.security.SecurityRuleCtx

class SecurityRuleCtxAction(private val actionCtx: ActionCtx) : SecurityRuleCtx {

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
}