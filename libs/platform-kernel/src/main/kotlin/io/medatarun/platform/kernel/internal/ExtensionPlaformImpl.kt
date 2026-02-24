package io.medatarun.platform.kernel.internal

import io.medatarun.platform.kernel.*

class ExtensionPlaformImpl(
    private val extensions: List<MedatarunExtension>,
    private val serviceRegistry: MedatarunServiceRegistryImpl,
    private val config: MedatarunConfig
) : ExtensionPlatform {
    private val eventSystem = EventSystemImpl()
    override val extensionRegistry: ExtensionRegistry = createExtensionRegistry()

    fun createExtensionRegistry(): ExtensionRegistryImpl {
        val allExtensions = listOf(KernelSelfExtension()) + extensions

        // Create an extension registry and give it all known extensions,
        // including the kernel as an extension itself.
        // Nothing will be initialized yet, everything is empty.
        val extensionRegistry = ExtensionRegistryImpl(allExtensions, config, serviceRegistry)

        // Pushes core services into the service registry, so that services declared in extensions
        // can at least access the core services, including the service registry itself, and the extension registry.
        serviceRegistry.register(ExtensionRegistry::class, extensionRegistry)
        serviceRegistry.register(MedatarunServiceRegistry::class, serviceRegistry)
        serviceRegistry.register(EventSystem::class, eventSystem)

        // Now let's fetch all services from all extensions
        // We must respect the order since extensions can depend on each other.
        // Order respects the dependencies.
        extensions.forEach { extension ->
            extension.initServices(
                MedatarunServiceCtxImpl(serviceRegistry, MedatarunExtensionCtxConfigImpl(extension, config))
            )
        }

        // Finally register all contributions
        extensionRegistry.init()

        // And returns the extension registry itself fully built
        return extensionRegistry
    }

    class KernelSelfExtension : MedatarunExtension {
        override val id: ExtensionId = "kernel"
        override fun init(ctx: MedatarunExtensionCtx) {
            ctx.registerContributionPoint(this.id + ".startup", PlatformStartedListener::class)
        }
    }

}