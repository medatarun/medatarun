package io.medatarun.kernel

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
     * Find all contributions to a contribution point with origin.
     *
     * Meant to be used by services where you need to know where the
     * contributions come from (for example when you need to display errors in declarations)
     *
     * This shall not be used in React components because the returned list is not
     * guaranteed to always have strict equality (===) and therefore could provoke
     * unnecessary refreshes.
     */
    fun <CONTRIB : Any> findContributionsWithOrigin(extensionPoint: KClass<CONTRIB>): List<ContributionWithOrigin<CONTRIB>>
}

interface ContributionWithOrigin<CONTRIB : Any> {
    val extensionId: String
    val item: CONTRIB
}