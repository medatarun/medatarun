package io.medatarun.platform.kernel.internal

import io.medatarun.platform.kernel.Event
import io.medatarun.platform.kernel.EventNotifier
import io.medatarun.platform.kernel.EventObserver
import io.medatarun.platform.kernel.EventSystem
import kotlin.reflect.KClass

class EventSystemImpl: EventSystem {

    private val observers: MutableMap<KClass<*>, MutableList<EventObserver<*>>> = mutableMapOf()

    override fun <T: Event> createNotifier(clazz: KClass<T>): EventNotifier<T> {
        return object : EventNotifier<T> {
            override fun fire(evt: T) {
                observers[clazz]?.forEach { obs ->
                    @Suppress("UNCHECKED_CAST")
                    val observer: EventObserver<T> = obs as EventObserver<T>
                    observer.onEvent(evt)
                }
            }

        }
    }
    override fun <T: Event> registerObserver(clazz: KClass<T>, evtObserver: EventObserver<T>) {
        val list = observers.getOrPut(clazz, ) {mutableListOf()}
        list.add(evtObserver)
    }
}