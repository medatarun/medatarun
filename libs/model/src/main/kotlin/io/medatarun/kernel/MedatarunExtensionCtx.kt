package io.medatarun.kernel

import java.nio.file.Path
import kotlin.reflect.KClass

interface MedatarunExtensionCtx {
    /**
     * Resolves a path relative to application home directory (where medatarun binary is located)
     */
    fun resolveApplicationHomePath(relativePath: String): Path
    /**
     * Resolves a path relative to the user project
     */
    fun resolveProjectPath(relativePath: String?): Path
    /**
     * Resolves a path relative to Medatarun data directory inside the project path.
     * Usually <projectPath>/.medatarun/<relativePath>
     */
    fun resolveMedatarunPath(relativePath: String?): Path

    /**
     * Returns extension storage path and create. When [init] is true, the directory is created if it doesn't exist yet.
     */
    fun resolveExtensionStoragePath(init: Boolean = false): Path
    val config: MedatarunExtensionCtxConfig

    fun <CONTRIB : Any> registerContributionPoint(id: ContributionPointId, api: KClass<CONTRIB>)
    fun <INTERFACE : Any, IMPL : INTERFACE> register(kClass: KClass<INTERFACE>, instance: IMPL)

    /**
     * Creates a new resource locator to access files and URLs
     */
    fun createResourceLocator(): ResourceLocator


}