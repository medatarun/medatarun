package io.medatarun.auth.fixtures

import io.medatarun.actions.ports.needs.*
import io.medatarun.auth.actions.ActionPrincipalCtxAdapter
import io.medatarun.auth.actions.AuthAction
import io.medatarun.auth.actions.AuthEmbeddedActionsProvider
import io.medatarun.auth.domain.ActorNotFoundException
import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.ports.exposed.*
import io.medatarun.kernel.ExtensionRegistry
import kotlin.reflect.KClass

class AuthActionEnvTest(private val createAdmin: Boolean = true) {

    val env = AuthEnvTest(createAdmin = createAdmin)

    val provider = AuthEmbeddedActionsProvider()

    var actionCtx: ActionCtx = ActionCtxWithActor(this, null)

    fun <R> dispatch(action: AuthAction<R>): R {
        return provider.dispatch(action, actionCtx) as R
    }

    fun logout()  {
        this.actionCtx = ActionCtxWithActor(this, null)
    }

    fun asAdmin() {
        val actorService = env.actorService
        val actor = actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), env.adminUsername.value)
            ?: throw ActorNotFoundException()
        this.actionCtx = ActionCtxWithActor(this, actor)
    }

    fun asUser(username: Username) {
        val actorService = env.actorService
        val actor = actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), username.value)
            ?: throw ActorNotFoundException()
        this.actionCtx = ActionCtxWithActor(this, actor)
    }

    fun <T : Any> getService(type: KClass<T>): T {
        if (type == OidcService::class) return env.oidcService as T
        if (type == UserService::class) return env.userService as T
        if (type == OAuthService::class) return env.oauthService as T
        if (type == ActorService::class) return env.actorService as T
        if (type == BootstrapSecretLifecycle::class) return env.bootstrapSecretLifecycle as T
        throw IllegalStateException("Unknown service " + type)
    }

    class ActionCtxWithActor(
        private val closure: AuthActionEnvTest,
        private val actor: Actor?
    ) : ActionCtx {

        private val appPrincipal = if (actor==null) null else toAppPrincipal(actor)
        private val actionPrincipal = ActionPrincipalCtxAdapter.toActionPrincipalCtx(appPrincipal)


        override val extensionRegistry: ExtensionRegistry
            get() = throw IllegalStateException("Should not be called")

        override fun dispatchAction(req: ActionRequest): Any? {
            throw IllegalStateException("Should not be called")
        }

        override fun <T : Any> getService(type: KClass<T>): T = closure.getService(type)

        override val principal: ActionPrincipalCtx = actionPrincipal
    }
    companion object {

        private fun toAppPrincipal(actor: Actor): AppPrincipal {
            return object : AppPrincipal {
                override val id: AppPrincipalId = AppPrincipalId(actor.id.value.toString())
                override val issuer: String = actor.issuer
                override val subject: String = actor.subject
                override val isAdmin: Boolean = actor.roles.any { it.isAdminRole() }
                override val roles: List<AppPrincipalRole> = actor.roles.map(::toMedatarunPrincipalRole)
                override val fullname: String = actor.fullname
            }
        }

        private fun toMedatarunPrincipalRole(role: ActorRole): AppPrincipalRole {
            return object : AppPrincipalRole {
                override val key = role.key
            }
        }
    }
}