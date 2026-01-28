package io.medatarun.platform.kernel.internal

import io.medatarun.platform.kernel.*

class ExtensionPlaformImpl(
    private val extensions: List<MedatarunExtension>,
    private val config: MedatarunConfig
) : ExtensionPlatform {

    override val extensionRegistry: ExtensionRegistry = createExtensionRegistry()

    fun createExtensionRegistry(): ExtensionRegistryImpl {
        val allExtensions = listOf(KernelSelfExtension()) + extensions
        return ExtensionRegistryImpl(allExtensions, config).also { it.init() }
    }

    class KernelSelfExtension : MedatarunExtension {
        override val id: ExtensionId = "kernel"
        override fun init(ctx: MedatarunExtensionCtx) {
            ctx.registerContributionPoint(this.id + ".startup", PlatformStartedListener::class)
        }
    }

}