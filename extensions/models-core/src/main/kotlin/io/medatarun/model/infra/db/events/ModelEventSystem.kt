package io.medatarun.model.infra.db.events

import io.medatarun.model.adapters.jsonserializers.ModelJsonSerializers
import io.medatarun.model.infra.db.events.ModelEventStreamNumberManager
import io.medatarun.model.infra.db.events.ModelEventRegistryBuilder
import kotlinx.serialization.json.Json

class ModelEventSystem {
    val registry: ModelEventRegistry = ModelEventRegistryBuilder().build()
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