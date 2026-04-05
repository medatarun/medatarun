package io.medatarun.auth.adapters

import io.medatarun.auth.domain.ActorPermission
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorId
import java.time.Instant

data class ActorInMemory(
    override val id: ActorId,
    override val issuer: String,
    override val subject: String,
    override val fullname: String,
    override val email: String?,
    override val disabledDate: Instant?,
    override val createdAt: Instant,
    override val lastSeenAt: Instant
) : Actor