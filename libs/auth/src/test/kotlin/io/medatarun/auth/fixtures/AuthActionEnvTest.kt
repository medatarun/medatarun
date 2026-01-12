package io.medatarun.auth.fixtures

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.actions.ports.needs.AppPrincipal
import io.medatarun.auth.actions.AuthAction
import io.medatarun.auth.actions.AuthEmbeddedActionsProvider
import io.medatarun.auth.ports.exposed.*
import io.medatarun.kernel.ExtensionRegistry
import kotlin.reflect.KClass

class AuthActionEnvTest(private val createAdmin: Boolean = true) {

    val env = AuthEnvTest(createAdmin = createAdmin)

    val provider = AuthEmbeddedActionsProvider()

    fun <R> dispatch(action: AuthAction<R>): R {
        return provider.dispatch(action, buildActionCtx()) as R
    }
    fun <T : Any> getService(type: KClass<T>): T {
        if (type == OidcService::class) return env.oidcService as T
        if (type == UserService::class) return env.userService as T
        if (type == OAuthService::class) return env.oauthService as T
        if (type == ActorService::class) return env.actorService as T
        if (type == BootstrapSecretLifecycle::class) return env.bootstrapSecretLifecycle as T
        throw IllegalStateException("Unknown service "+type)
    }
    private fun buildActionCtx(): ActionCtx {
        val closure = this
        return object : ActionCtx {
            override val extensionRegistry: ExtensionRegistry
                get() = throw IllegalStateException("Should not be called")

            override fun dispatchAction(req: ActionRequest): Any? {
                throw IllegalStateException("Should not be called")
            }

            override fun <T : Any> getService(type: KClass<T>): T {
                return closure.getService(type)
            }

            override val principal: ActionPrincipalCtx
                get() {
                    return object : ActionPrincipalCtx {
                        override fun ensureIsAdmin() {
                            TODO("Not yet implemented")
                        }

                        override fun ensureSignedIn(): AppPrincipal {
                            TODO("Not yet implemented")
                        }

                        override val principal: AppPrincipal?
                            get() = TODO("Not yet implemented")

                    }
                }

        }
    }
}