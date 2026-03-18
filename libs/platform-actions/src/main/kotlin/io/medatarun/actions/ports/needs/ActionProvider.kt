package io.medatarun.actions.ports.needs

import io.medatarun.actions.domain.ActionDescriptor
import kotlin.reflect.KClass

interface ActionProvider<C: Any> {
    val actionGroupKey: String
    fun findCommandClass(): KClass<C>?
    fun dispatch(action: C, actionCtx: ActionCtx): Any?
    fun findActions(): List<ActionDescriptor> = emptyList()
}