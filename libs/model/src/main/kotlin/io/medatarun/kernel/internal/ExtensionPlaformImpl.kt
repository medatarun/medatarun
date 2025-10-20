package io.medatarun.kernel.internal

import io.medatarun.kernel.ExtensionPlatform
import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.kernel.MedatarunConfig
import io.medatarun.kernel.MedatarunExtension

class ExtensionPlaformImpl(
    private val extensions: List<MedatarunExtension>,
    private val config: MedatarunConfig
) : ExtensionPlatform {
    override val extensionRegistry: ExtensionRegistry = ExtensionRegistryImpl(extensions, config)
        .also { it.init() }
}