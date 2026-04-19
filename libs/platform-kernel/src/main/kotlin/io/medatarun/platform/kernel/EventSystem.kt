package io.medatarun.platform.kernel

import kotlin.reflect.KClass


interface EventSystem: Service {

    fun <T : Event> registerObserver(clazz: KClass<T>, evtObserver: EventObserver<T>)
    fun <T : Event> registerObserver(clazz: KClass<T>, block: (T) -> Unit)
    fun <T : Event> createNotifier(clazz: KClass<T>): EventNotifier<T>
}
