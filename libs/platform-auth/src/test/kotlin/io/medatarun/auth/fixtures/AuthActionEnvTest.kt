package io.medatarun.auth.fixtures

import io.medatarun.actions.domain.ActionInstanceId
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.actions.ports.needs.ActionRequestCtx
import io.medatarun.auth.actions.ActionPrincipalCtxAdapter
import io.medatarun.auth.actions.AuthAction
import io.medatarun.auth.actions.AuthEmbeddedActionsProvider
import io.medatarun.auth.adapters.ActorRoleAdapters
import io.medatarun.auth.adapters.AppActorIdAdapter
import io.medatarun.auth.domain.ActorNotFoundException
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.user.Username
import io.medatarun.security.AppActorId
import io.medatarun.security.AppPermission
import io.medatarun.security.AppPrincipal
import io.medatarun.type.commons.id.Id
import kotlin.reflect.KClass

class AuthActionEnvTest(
    private val createAdmin: Boolean = true,
    otherPermissions: Set<AuthEnvTest.TestOtherPermission> = emptySet(),
) {
    val env = AuthEnvTest(createAdmin = createAdmin, otherPermissions=otherPermissions)

    val provider = AuthEmbeddedActionsProvider(
        env.userService, env.oidcService, env.oauthService, env.actorService, env.authClockTests
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
        override val requestCtx: ActionRequestCtx
            get() = object : ActionRequestCtx {
                override val principalCtx: ActionPrincipalCtx = actionPrincipal
                override val source: String = "tests"
            }
    }

    companion object {

        private fun toAppPrincipal(actor: Actor): AppPrincipal {
            return object : AppPrincipal {
                override val id: AppActorId = AppActorIdAdapter.toAppActorId(actor.id)
                override val issuer: String = actor.issuer
                override val subject: String = actor.subject
                override val isAdmin: Boolean = actor.roles.any { it.isAdminPermission() }
                override val permissions: List<AppPermission> = actor.roles.map(ActorRoleAdapters::toAppPermission)
                override val fullname: String = actor.fullname
            }
        }
    }
}
