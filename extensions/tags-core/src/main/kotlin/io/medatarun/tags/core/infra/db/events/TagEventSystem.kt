package io.medatarun.tags.core.infra.db.events

import io.medatarun.storage.eventsourcing.StorageCmd
import io.medatarun.storage.eventsourcing.StorageEventDescriptor
import io.medatarun.storage.eventsourcing.StorageEventJsonCodec
import io.medatarun.storage.eventsourcing.StorageEventRegistry
import io.medatarun.storage.eventsourcing.StorageEventRegistryBuilder
import io.medatarun.storage.eventsourcing.StorageEventSystem
import io.medatarun.tags.core.adapters.jsonserializers.TagsJsonSerializers
import io.medatarun.tags.core.ports.needs.TagStorageCmd
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

class TagEventSystem: StorageEventSystem<TagStorageCmd> {

    override val storageCmdRootClass: KClass<out StorageCmd> = TagStorageCmd::class

    private val registryEntries = StorageEventRegistryBuilder<TagStorageCmd>()
        .build(TagStorageCmd::class)

    val registry: StorageEventRegistry<TagStorageCmd> =
        StorageEventRegistry("TagEventRegistry", registryEntries)

    val jsonSerializer = Json {
        prettyPrint = false
        ignoreUnknownKeys = false
        encodeDefaults = true
        serializersModule = TagsJsonSerializers.module()
    }
    override val codec: StorageEventJsonCodec<TagStorageCmd> =
        StorageEventJsonCodec(registry = registry, json = jsonSerializer)

    val recordFactory = TagEventRecordFactory(codec)

    val eventStreamRevisionManager = TagEventStreamRevisionManager()

    override fun findAllDescriptors(): Collection<StorageEventDescriptor<out TagStorageCmd>> {
        return registry.findAllDescriptor()
    }

    override fun upscale(cmdAnyVersion: TagStorageCmd): List<TagStorageCmd> {
        return when (cmdAnyVersion) {
            is TagStorageCmd -> listOf(cmdAnyVersion)
        }
    }
}
