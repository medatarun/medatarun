package io.medatarun.actions.infra.db

import java.time.Instant

interface ActionAuditClock {
    fun now(): Instant
}
