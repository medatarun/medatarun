package io.medatarun.kernel

import kotlin.reflect.KClass

interface MedatarunExtensionCtx: MedatarunExtensionCtxConfig {


    fun <CONTRIB : Any> registerContributionPoint(id: ContributionPointId, api: KClass<CONTRIB>)
    fun <INTERFACE : Any, IMPL : INTERFACE> register(kClass: KClass<INTERFACE>, instance: IMPL)


}