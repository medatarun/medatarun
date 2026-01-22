package io.medatarun.platform.kernel.internal

import io.medatarun.platform.kernel.ExtensionPlatform
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.MedatarunConfig
import io.medatarun.platform.kernel.MedatarunExtension

class ExtensionPlaformImpl(
    extensions: List<MedatarunExtension>,
    config: MedatarunConfig
) : ExtensionPlatform {

    override val extensionRegistry: ExtensionRegistry = ExtensionRegistryImpl(extensions, config).also { it.init() }


}