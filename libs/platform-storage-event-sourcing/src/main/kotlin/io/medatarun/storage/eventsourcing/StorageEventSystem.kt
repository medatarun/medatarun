package io.medatarun.storage.eventsourcing

import kotlin.reflect.KClass

interface StorageEventSystem<CMD: StorageCmd> {
    val storageCmdRootClass: KClass<out StorageCmd>
    val codec:StorageEventJsonCodec<CMD>
    fun findAllDescriptors(): Collection<StorageEventDescriptor<out CMD>>
    fun upscale(cmdAnyVersion: CMD): List<CMD>
}