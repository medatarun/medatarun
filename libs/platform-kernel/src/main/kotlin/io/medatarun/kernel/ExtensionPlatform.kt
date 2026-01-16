package io.medatarun.kernel


/**
 * Extension platform exposes the core internal services.
 *
 * Use createExtensionPlatform(extensions) to create the platform
 */
interface ExtensionPlatform {
    /**
     * Gets initialized and running extension registry
     */
    val extensionRegistry: ExtensionRegistry

}