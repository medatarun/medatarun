package io.medatarun.actions.ports.needs

import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.exposed.ModelHumanPrinter
import io.medatarun.model.ports.exposed.ModelQueries
import kotlin.reflect.KClass

interface ActionCtx {
    val extensionRegistry: ExtensionRegistry
    val modelCmds: ModelCmds
    val modelQueries: ModelQueries
    val modelHumanPrinter: ModelHumanPrinter
    fun dispatchAction(req: ActionRequest): Any?
    fun <T : Any> getService(type: KClass<T>): T
}

inline fun <reified T : Any> ActionCtx.getService(): T {
    return this.getService(T::class)
}