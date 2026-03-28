package io.medatarun.model.infra.db.events

import io.medatarun.model.adapters.jsonserializers.ModelJsonSerializers
import io.medatarun.model.ports.needs.ModelStorageCmd
import io.medatarun.storage.eventsourcing.StorageEventJsonCodec
import io.medatarun.storage.eventsourcing.StorageEventRegistry
import io.medatarun.storage.eventsourcing.StorageEventRegistryBuilder
import kotlinx.serialization.json.Json

class ModelEventSystem {

    private val registryEntries = StorageEventRegistryBuilder<ModelStorageCmd>()
        .build(ModelStorageCmd::class)

    private val storageEventRegistry: StorageEventRegistry<ModelStorageCmd> =
        StorageEventRegistry("ModelEventRegistry", registryEntries)

    val registry = ModelEventKnownTypes(storageEventRegistry)

    val jsonSerializer = Json {
        prettyPrint = false
        ignoreUnknownKeys = false
        encodeDefaults = true
        serializersModule = ModelJsonSerializers.module()
    }

    val codec: StorageEventJsonCodec<ModelStorageCmd> =
        StorageEventJsonCodec(registry = storageEventRegistry, json = jsonSerializer)

    val recordFactory = ModelEventRecordFactory(codec)

    val eventStreamNumberManager = ModelEventStreamNumberManager()

}