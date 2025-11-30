package io.medatarun.kernel

import java.nio.file.Path
import kotlin.reflect.KClass

interface MedatarunExtensionCtx {
    fun resolveProjectPath(relativePath: String?): Path
    fun resolveMedatarunPath(relativePath: String?): Path

    /**
     * Returns extension storage path and create. When [init] is true, the directory is created if it doesn't exist yet.
     */
    fun resolveExtensionStoragePath(init: Boolean = false): Path
    val config: MedatarunExtensionCtxConfig

    fun <CONTRIB : Any> registerContributionPoint(id: ContributionPointId, api: KClass<CONTRIB>)
    fun <INTERFACE : Any, IMPL : INTERFACE> register(kClass: KClass<INTERFACE>, instance: IMPL)

}