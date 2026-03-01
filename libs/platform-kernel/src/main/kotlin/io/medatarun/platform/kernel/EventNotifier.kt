package io.medatarun.platform.kernel

interface EventNotifier<T: Event> {
    /**
     * Fire this event and waits that all observers where handled (synchronous)
     */
    fun fire(evt: T)
}