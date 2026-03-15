package io.medatarun.model.infra.db.events

import io.medatarun.model.adapters.jsonserializers.ModelJsonSerializers
import io.medatarun.model.infra.db.ModelEventRegistry
import io.medatarun.model.infra.db.ModelEventStreamNumberManager
import io.medatarun.model.infra.db.ModelStorageEventRegistryBuilder
import kotlinx.serialization.json.Json

class ModelEventSystem {
    val registry: ModelEventRegistry = ModelStorageEventRegistryBuilder().build()
    val jsonSerializer = Json {
        prettyPrint = false
        ignoreUnknownKeys = false
        encodeDefaults = true
        serializersModule = ModelJsonSerializers.module()
    }
    val codec = ModelEventJsonCodec(
        registry = registry,
        json = jsonSerializer
    )
    val recordFactory = ModelEventRecordFactory(codec)
    val eventStreamNumberManager = ModelEventStreamNumberManager()

}