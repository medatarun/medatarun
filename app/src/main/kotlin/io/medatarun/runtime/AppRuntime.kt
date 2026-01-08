package io.medatarun.runtime

import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.kernel.MedatarunServiceRegistry
import io.medatarun.runtime.internal.AppRuntimeConfig

interface AppRuntime {

    val config: AppRuntimeConfig
    val extensionRegistry: ExtensionRegistry
    val services: MedatarunServiceRegistry

}