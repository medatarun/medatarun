package io.medatarun.kernel

typealias ExtensionId = String

interface MedatarunExtension {
    val id: ExtensionId
    fun init(ctx: MedatarunExtensionCtx)
    fun initServices(ctx: MedatarunServiceCtx): Unit {
        // default void implementation
    }
}