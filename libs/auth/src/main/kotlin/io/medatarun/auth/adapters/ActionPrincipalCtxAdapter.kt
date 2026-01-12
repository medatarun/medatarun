package io.medatarun.auth.adapters

import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.AppPrincipal
import io.medatarun.auth.domain.AuthUnauthorizedException

object ActionPrincipalCtxAdapter {
    fun toActionPrincipalCtx(appPrincipal: AppPrincipal?): ActionPrincipalCtx {
        return object: ActionPrincipalCtx {

            override val principal: AppPrincipal? = appPrincipal
            override fun ensureSignedIn(): AppPrincipal {
                if (appPrincipal == null) throw AuthUnauthorizedException()
                return appPrincipal
            }

            override fun ensureIsAdmin() {
                if (appPrincipal == null) throw AuthUnauthorizedException()
                if (!appPrincipal.isAdmin) throw AuthUnauthorizedException()
            }
        }
    }
}