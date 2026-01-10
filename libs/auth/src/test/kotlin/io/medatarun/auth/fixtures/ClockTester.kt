package io.medatarun.auth.fixtures

import io.medatarun.auth.ports.needs.AuthClock
import java.time.Instant

class ClockTester(var staticNow: Instant = Instant.now()) : AuthClock {
    override fun now(): Instant = staticNow
}