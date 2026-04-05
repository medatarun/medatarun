package io.medatarun.auth.adapters

import io.medatarun.auth.domain.ActorPermission
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorWithPermissions

class ActorWithPermissionsInMemory(
    private val actor: Actor,
    override val permissions: Set<ActorPermission>
) : ActorWithPermissions, Actor by actor