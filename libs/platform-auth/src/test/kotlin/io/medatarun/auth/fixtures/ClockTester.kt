package io.medatarun.auth.fixtures

import io.medatarun.auth.ports.needs.AuthClock
import java.time.Instant

/**
 * Clock implémentation for tests
 */
class ClockTester(
    /**
     * The default is to provide "now" as something fresh.
     *
     * This helps when we need to work with external libraries that internally use the system clock.
     */
    var staticNow: Instant = Instant.now()
) : AuthClock {
    /**
     * Gets the current "now"
     */
    override fun now(): Instant = staticNow
}