package io.medatarun.auth.adapters

import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.security.AppActor
import io.medatarun.security.AppActorId
import io.medatarun.security.AppActorResolver

/**
 * Registered as the resolver for actors in application-wide contexts.
 *
 * Other modules ask to resolve actors to security layer, here we bridge
 * the gap by implementing the resolver
 */
class AppActorResolverAuth(private val actorService: ActorService) : AppActorResolver {
    override fun resolve(appActorId: AppActorId): AppActor? {
        val actorId = AppActorIdAdapter.fromAppActorId(appActorId)
        return actorService.findByIdOptional(actorId)?.let {
            object : AppActor {
                override val id: AppActorId
                    get() = appActorId
                override val displayName: String
                    get() = it.fullname
            }
        }

    }

}