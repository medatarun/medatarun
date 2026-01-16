package io.medatarun.runtime

import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.MedatarunServiceRegistry
import io.medatarun.runtime.internal.AppRuntimeConfig

interface AppRuntime {

    val config: AppRuntimeConfig
    val extensionRegistry: ExtensionRegistry
    val services: MedatarunServiceRegistry

}