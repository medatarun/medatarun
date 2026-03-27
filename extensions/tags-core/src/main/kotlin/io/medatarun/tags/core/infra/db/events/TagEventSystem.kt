package io.medatarun.tags.core.infra.db.events

import io.medatarun.storage.eventsourcing.StorageEventJsonCodec
import io.medatarun.storage.eventsourcing.StorageEventRegistry
import io.medatarun.storage.eventsourcing.StorageEventRegistryBuilder
import io.medatarun.tags.core.adapters.jsonserializers.TagsJsonSerializers
import io.medatarun.tags.core.ports.needs.TagStorageCmd
import kotlinx.serialization.json.Json

class TagEventSystem {

    private val registryEntries = StorageEventRegistryBuilder<TagStorageCmd>()
        .build(TagStorageCmd::class)

    private val storageEventRegistry: StorageEventRegistry<TagStorageCmd> =
        StorageEventRegistry("TagEventRegistry", registryEntries)

    val jsonSerializer = Json {
        prettyPrint = false
        ignoreUnknownKeys = false
        encodeDefaults = true
        serializersModule = TagsJsonSerializers.module()
    }
    val codec: StorageEventJsonCodec<TagStorageCmd> =
        StorageEventJsonCodec(registry = storageEventRegistry, json = jsonSerializer)

    val recordFactory = TagEventRecordFactory(codec)

    val eventStreamRevisionManager = TagEventStreamRevisionManager()
}
