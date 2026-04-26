package io.medatarun.auth.fixtures

import io.medatarun.auth.ports.needs.AuthClock
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Clock implémentation for tests
 */
class ClockTester(
    /**
     * The default is to provide "now" as something fresh.
     *
     * This helps when we need to work with external libraries that internally
     * use the system clock.
     *
     * Be careful that we truncate to miliseconds. This is for comparing test
     * values with database values, because when we store
     * in the database, the nanoseconds are lost, then the tests usually fail.
     */
    var staticNow: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

) : AuthClock {
    /**
     * Gets the current "now"
     */
    override fun now(): Instant = staticNow

}