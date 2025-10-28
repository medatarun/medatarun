package io.medatarun.resources

import kotlin.reflect.KClass

interface ResourceContainer {
    fun findCommandClass(): KClass<out Any>?
    fun dispatch(cmd: Any): Any?
}