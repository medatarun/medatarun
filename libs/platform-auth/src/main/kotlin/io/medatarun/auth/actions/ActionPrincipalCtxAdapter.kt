package io.medatarun.auth.actions

import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.auth.domain.AuthNotAuthenticatedException
import io.medatarun.auth.domain.AuthNotAuthorizedException
import io.medatarun.security.AppPrincipal

object ActionPrincipalCtxAdapter {
    fun toActionPrincipalCtx(appPrincipal: AppPrincipal?): ActionPrincipalCtx {
        return object: ActionPrincipalCtx {

            override val principal: AppPrincipal? = appPrincipal
            override fun ensureSignedIn(): AppPrincipal {
                if (appPrincipal == null) throw AuthNotAuthenticatedException()
                return appPrincipal
            }

            override fun ensureIsAdmin() {
                if (appPrincipal == null) throw AuthNotAuthenticatedException()
                if (!appPrincipal.isAdmin) throw AuthNotAuthorizedException()
            }
        }
    }
}