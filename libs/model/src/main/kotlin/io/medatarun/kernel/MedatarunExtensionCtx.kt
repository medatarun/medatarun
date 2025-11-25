package io.medatarun.kernel

import java.nio.file.Path
import kotlin.reflect.KClass

interface MedatarunExtensionCtx {
    fun resolveProjectPath(relativePath: String?): Path
    val config: MedatarunExtensionCtxConfig

    fun <CONTRIB : Any> registerContributionPoint(id: ContributionPointId, api: KClass<CONTRIB>)
    fun <INTERFACE : Any, IMPL : INTERFACE> register(kClass: KClass<INTERFACE>, instance: IMPL)

}