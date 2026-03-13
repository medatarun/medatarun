package io.medatarun.auth.fixtures

import io.medatarun.actions.domain.ActionInstanceId
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.auth.actions.ActionPrincipalCtxAdapter
import io.medatarun.auth.actions.AuthAction
import io.medatarun.auth.actions.AuthEmbeddedActionsProvider
import io.medatarun.auth.domain.ActorNotFoundException
import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.user.Username
import io.medatarun.security.AppPrincipal
import io.medatarun.security.AppPrincipalId
import io.medatarun.security.AppPrincipalRole
import io.medatarun.type.commons.id.Id
import kotlin.reflect.KClass

class AuthActionEnvTest(
    private val createAdmin: Boolean = true,
    private val otherRoles: Set<String> = emptySet(),
) {

    val env = AuthEnvTest(createAdmin = createAdmin, otherRoles = otherRoles)

    val provider = AuthEmbeddedActionsProvider(
        env.userService, env.oidcService, env.oauthService, env.actorService
    )

    var actionCtx: ActionCtx = ActionCtxWithActor(null)

    fun <R> dispatch(action: AuthAction<R>): R {
        return provider.dispatch(action, actionCtx) as R
    }

    fun logout() {
        this.actionCtx = ActionCtxWithActor(null)
    }

    fun asAdmin() {
        val actorService = env.actorService
        val actor = actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), env.adminUsername.value)
            ?: throw ActorNotFoundException()
        this.actionCtx = ActionCtxWithActor(actor)
    }

    fun asUser(username: Username) {
        val actorService = env.actorService
        val actor = actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), username.value)
            ?: throw ActorNotFoundException()
        this.actionCtx = ActionCtxWithActor(actor)
    }

    fun <T : Any> getService(type: KClass<T>): T = env.runtime.services.getService(type)

    class ActionCtxWithActor(
        private val actor: Actor?
    ) : ActionCtx {
        override val actionInstanceId = Id.generate(::ActionInstanceId)
        private val appPrincipal = if (actor == null) null else toAppPrincipal(actor)
        private val actionPrincipal = ActionPrincipalCtxAdapter.toActionPrincipalCtx(appPrincipal)

        override fun dispatchAction(req: ActionRequest): Any? {
            throw IllegalStateException("Should not be called")
        }

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
