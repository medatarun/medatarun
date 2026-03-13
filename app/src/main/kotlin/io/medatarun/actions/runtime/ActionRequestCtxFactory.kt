package io.medatarun.actions.runtime

import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequestCtx
import io.medatarun.auth.actions.ActionPrincipalCtxAdapter
import io.medatarun.security.AppPrincipal

class ActionRequestCtxFactory() {
    fun create(principal: AppPrincipal?): ActionRequestCtx {
        return object : ActionRequestCtx {

            override val principal: ActionPrincipalCtx = ActionPrincipalCtxAdapter.toActionPrincipalCtx(principal)
        }
    }
}