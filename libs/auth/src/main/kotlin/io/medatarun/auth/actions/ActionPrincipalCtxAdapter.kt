package io.medatarun.auth.actions

import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.auth.domain.AuthUnauthorizedException
import io.medatarun.security.AppPrincipal

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