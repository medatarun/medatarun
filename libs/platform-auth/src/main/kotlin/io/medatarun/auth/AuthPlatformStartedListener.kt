package io.medatarun.auth

import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.platform.kernel.PlatformStartedCtx
import io.medatarun.platform.kernel.PlatformStartedListener

/**
 * Launched after database migrations once per application startup
 */
class AuthPlatformStartedListener(
    private val actorService: ActorService
) : PlatformStartedListener {
    override fun onPlatformStarted(ctx: PlatformStartedCtx) {
        actorService.syncManagedRoles()
    }

}
