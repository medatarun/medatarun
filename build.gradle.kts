import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

/**
 * This is the root build of gradle.
 *
 * Only put here additional plugins that we want for the whole project
 */
plugins {
    /**
     * Add a new task to Gradle "dependencyUpdate" that acts like "npm outdated"
     *
     * https://github.com/ben-manes/gradle-versions-plugin
     */
    alias(libs.plugins.gradleVersionsPlugin)
}

/**
 * Configure task "dependencyUpdates" to generate report in build/dependencyUpadtes/report.html
 * and focus only on releases
 */
tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return isStable.not()
    }
    outputFormatter = "html"
    revision = "release"
    rejectVersionIf { isNonStable(candidate.version) && !isNonStable(currentVersion) }
}