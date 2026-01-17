package io.medatarun.platform.kernel

typealias ExtensionId = String

interface MedatarunExtension {
    val id: ExtensionId
    fun init(ctx: MedatarunExtensionCtx)
    fun initServices(ctx: MedatarunServiceCtx) {
        // default void implementation
    }
}
