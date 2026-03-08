package io.medatarun.platform.kernel

typealias ExtensionId = String

/**
 * Contract that extensions must implement
 */
interface MedatarunExtension {
    /**
     * Unique identifier of extension across all extensions registered on the platform
     */
    val id: ExtensionId

    /**
     * This is where services are registered.
     *
     * This is called first in the lifecycle of an extension.
     *
     * Important point: contributions are not registered yet, so you may reference the extension registry
     * but not read its content now because contributions had not been populated yet.
     *
     * If needed, use lazy loading when you parse contribution contents.
     */
    fun initServices(ctx: MedatarunServiceCtx) {
        // default void implementation
    }

    /**
     * This is where contributions to contribution points are registered.
     *
     * This is called **after** [initServices] of all extensions had been called, so after
     * [initServices] of your extension had been called too. It means that services registered
     * in [initServices] are now available.
     */
    fun initContributions(ctx: MedatarunExtensionCtx) {
        // default void implementation
    }

}
