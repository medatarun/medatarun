package io.medatarun.platform.db.testkit

import org.junit.jupiter.api.extension.ExtendWith

/**
 * Enables PostgreSQL test container lifecycle for a test class.
 *
 * Behavior:
 * - no-op when DB engine is not configured as postgresql
 * - starts one PostgreSQL container for the full JUnit test plan
 * - recreates the dedicated schema before each test
 * - exposes JDBC properties through JVM system properties
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(EnableDatabaseTestsExtension::class)
annotation class EnableDatabaseTests(
    val mode: DatabaseRecreateMode = DatabaseRecreateMode.PER_TEST
)

enum class DatabaseRecreateMode {
    PER_TEST,
    ONCE_PER_CLASS
}
