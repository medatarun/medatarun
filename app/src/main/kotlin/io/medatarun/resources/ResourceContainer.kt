package io.medatarun.resources

import kotlin.reflect.KClass

interface ResourceContainer<C: Any> {
    fun findCommandClass(): KClass<C>?
    fun dispatch(cmd: C): Any?
}