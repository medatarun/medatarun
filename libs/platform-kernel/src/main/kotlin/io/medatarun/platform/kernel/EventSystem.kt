package io.medatarun.platform.kernel

import kotlin.reflect.KClass

interface EventSystem {

    fun <T : Event> registerObserver(clazz: KClass<T>, evtObserver: EventObserver<T>)
    fun <T : Event> createNotifier(clazz: KClass<T>): EventNotifier<T>
}
