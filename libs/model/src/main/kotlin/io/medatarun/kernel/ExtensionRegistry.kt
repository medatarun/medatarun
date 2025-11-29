package io.medatarun.kernel

import io.medatarun.kernel.internal.ContributionImpl
import io.medatarun.kernel.internal.ContributionPoint
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass

/**
 * Extension registry references all declared extensions.
 *
 * Once build, stores all contributions from all extensions in all contribution
 * points.
 *
 * User can query the registry to get all contributions for a particular
 * contribution point.
 */
interface ExtensionRegistry {
    /**
     * Find all contributions to a contribution point.
     *
     * Returned list is flat (meaning that your can't known the origin extension of the contribution).
     *
     * Meant to be used in cases where speed is required because it returns the pre-built list of contributions
     * which doesn't change since the extension platform contruction (and also will always be === compatible.)
     *
     */
    fun <CONTRIB : Any> findContributionsFlat(api: KClass<CONTRIB>): List<CONTRIB>;

    /**
     * Returns a human readable report for inspection
     */
    fun inspectHumanReadable(): String

    /**
     * Returns configuration
     */
    fun inspectJson(): JsonObject
}
