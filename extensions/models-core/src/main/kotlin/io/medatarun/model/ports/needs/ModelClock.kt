package io.medatarun.model.ports.needs

import java.time.Instant

interface ModelClock {
    fun now(): Instant
}
