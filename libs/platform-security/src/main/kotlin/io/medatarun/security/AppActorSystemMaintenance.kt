package io.medatarun.security

import java.util.*

object AppActorSystemMaintenance : AppActor {

    // Never change this id, never. It serves as identifier for system maintenance
    const val SYSTEM_MAINTENANCE_ACTOR_ID_STR = "01941f29-7c00-7000-9a65-67088ebcbabd"
    val SYSTEM_MAINTENANCE_ACTOR_ID: UUID = UUID.fromString(SYSTEM_MAINTENANCE_ACTOR_ID_STR)

    override val id: AppActorId = AppActorId(SYSTEM_MAINTENANCE_ACTOR_ID)
    override val displayName: String = "System maintenance"
}