package io.medatarun.auth.adapters

import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.security.AppActorId

object AppActorIdAdapter {
    fun fromAppActorId(appActorId: AppActorId): ActorId = ActorId(appActorId.value)
    fun toAppActorId(actorId: ActorId): AppActorId = AppActorId(actorId.value)
}