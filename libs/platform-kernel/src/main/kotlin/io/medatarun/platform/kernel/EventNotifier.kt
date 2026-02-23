package io.medatarun.platform.kernel

interface EventNotifier<T: Event> {
    fun fire(evt: T)
}