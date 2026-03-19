package io.medatarun.actions.runtime

import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequestCtx
import io.medatarun.auth.actions.ActionPrincipalCtxAdapter
import io.medatarun.security.AppPrincipal

class ActionRequestCtxFactory {
    fun create(principal: AppPrincipal?, source: String): ActionRequestCtx {
        return object : ActionRequestCtx {

            override val principalCtx: ActionPrincipalCtx = ActionPrincipalCtxAdapter.toActionPrincipalCtx(principal)
            override val source: String = source
        }
    }
}
