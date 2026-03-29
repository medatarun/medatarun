package io.medatarun.security

import io.medatarun.lang.uuid.UuidUtils
import java.util.*

object AppActorSystemMaintenance : AppActor {

    // Never change this id, never. It serves as identifier for system maintenance
    const val SYSTEM_MAINTENANCE_ACTOR_ID_STR = "01941f29-7c00-7000-9a65-67088ebcbabd"
    const val SYSTEM_MAINTENANCE_ISSUER = "urn:medatarun:system"
    const val SYSTEM_MAINTENANCE_SUBJECT = "system-maintenance"
    val SYSTEM_MAINTENANCE_ACTOR_ID: UUID = UuidUtils.fromStringSafe(SYSTEM_MAINTENANCE_ACTOR_ID_STR)

    override val id: AppActorId = AppActorId(SYSTEM_MAINTENANCE_ACTOR_ID)
    override val displayName: String = "System maintenance"
}
