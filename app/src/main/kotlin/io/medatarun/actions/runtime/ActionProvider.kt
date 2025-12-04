package io.medatarun.actions.runtime

import kotlin.reflect.KClass

interface ActionProvider<C: Any> {
    fun findCommandClass(): KClass<C>?
    fun dispatch(cmd: C, actionCtx: ActionCtx): Any?
}