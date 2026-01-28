package io.medatarun.platform.kernel

interface PlatformStartedCtx {
    val extensionRegistry: ExtensionRegistry
    val services: MedatarunServiceRegistry
}