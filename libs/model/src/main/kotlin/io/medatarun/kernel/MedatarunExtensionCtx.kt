package io.medatarun.kernel

import io.medatarun.model.model.ModelRepository
import java.nio.file.Path
import kotlin.reflect.KClass

interface MedatarunExtensionCtx {
    fun getConfigProperty(key: String): String?
    fun getConfigProperty(key: String, defaultValue: String): String
    fun resolveProjectPath(relativePath: String): Path
    fun registerRepository(repo: ModelRepository)
    fun <CONTRIB : Any> registerContributionPoint(id: ContributionPointId, api: KClass<CONTRIB>)
    fun <INTERFACE : Any, IMPL : INTERFACE> register(kClass: KClass<INTERFACE>, instance: IMPL)
}