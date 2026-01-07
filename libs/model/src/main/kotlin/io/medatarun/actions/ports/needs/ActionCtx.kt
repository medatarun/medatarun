package io.medatarun.actions.ports.needs

import io.medatarun.kernel.ExtensionRegistry
import kotlin.reflect.KClass

interface ActionCtx {
    val extensionRegistry: ExtensionRegistry
    fun dispatchAction(req: ActionRequest): Any?
    fun <T : Any> getService(type: KClass<T>): T
    val principal: ActionPrincipalCtx
}

inline fun <reified T : Any> ActionCtx.getService(): T {
    return this.getService(T::class)
}