package io.medatarun.platform.kernel

interface EventObserver<T: Event> {
    fun onEvent(evt: T)
}