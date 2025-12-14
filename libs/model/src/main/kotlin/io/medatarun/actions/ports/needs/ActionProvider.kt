package io.medatarun.actions.ports.needs

import kotlin.reflect.KClass

interface ActionProvider<C: Any> {
    val actionGroupKey: String
    fun findCommandClass(): KClass<C>?
    fun dispatch(cmd: C, actionCtx: ActionCtx): Any?
}