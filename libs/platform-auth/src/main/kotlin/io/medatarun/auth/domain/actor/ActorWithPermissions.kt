package io.medatarun.auth.domain.actor

import io.medatarun.auth.domain.ActorPermission

interface ActorWithPermissions: Actor {
    val permissions: Set<ActorPermission>
}