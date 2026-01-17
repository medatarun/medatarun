package io.medatarun.platform.kernel


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