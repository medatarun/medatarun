package io.medatarun.actions.ports.needs

import io.medatarun.actions.domain.ActionCmdDescriptor
import kotlin.reflect.KClass

interface ActionProvider<C: Any> {
    val actionGroupKey: String
    fun findCommandClass(): KClass<C>?
    fun dispatch(cmd: C, actionCtx: ActionCtx): Any?
    fun findActions(): List<ActionCmdDescriptor> = emptyList()
}