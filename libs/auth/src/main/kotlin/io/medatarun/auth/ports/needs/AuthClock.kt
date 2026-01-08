package io.medatarun.auth.ports.needs

import java.time.Instant


interface AuthClock {
    fun now(): Instant
}