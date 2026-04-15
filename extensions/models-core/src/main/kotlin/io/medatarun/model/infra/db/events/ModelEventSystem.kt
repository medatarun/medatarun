package io.medatarun.model.infra.db.events

import io.medatarun.model.adapters.jsonserializers.ModelJsonSerializers
import io.medatarun.model.ports.needs.ModelStorageCmd
import io.medatarun.model.ports.needs.ModelStorageCmdAnyVersion
import io.medatarun.model.ports.needs.ModelStorageCmdOld
import io.medatarun.storage.eventsourcing.StorageEventJsonCodec
import io.medatarun.storage.eventsourcing.StorageEventRegistry
import io.medatarun.storage.eventsourcing.StorageEventRegistryBuilder
import kotlinx.serialization.json.Json

class ModelEventSystem {


    private val registryEntries = StorageEventRegistryBuilder<ModelStorageCmdAnyVersion>()
        .build(ModelStorageCmdAnyVersion::class)

    private val storageEventRegistry: StorageEventRegistry<ModelStorageCmdAnyVersion> =
        StorageEventRegistry("ModelEventRegistry", registryEntries)

    private val upscaler = ModelEventUpscaler()

    val registry = ModelEventKnownTypes(storageEventRegistry)

    val jsonSerializer = Json {
        prettyPrint = false
        ignoreUnknownKeys = false
        encodeDefaults = true
        serializersModule = ModelJsonSerializers.module()
    }

    val codec: StorageEventJsonCodec<ModelStorageCmdAnyVersion> =
        StorageEventJsonCodec(registry = storageEventRegistry, json = jsonSerializer)

    val recordFactory = ModelEventRecordFactory(codec)

    val eventStreamNumberManager = ModelEventStreamNumberManager()

    fun upscale(cmdAnyVersion: ModelStorageCmdAnyVersion): List<ModelStorageCmd> {
        return when(cmdAnyVersion) {
            is ModelStorageCmdOld -> upscaler.upscale(cmdAnyVersion)
            is ModelStorageCmd -> listOf(cmdAnyVersion)
        }
    }

}