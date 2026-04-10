package io.medatarun.ext.modeljson.internal

import io.medatarun.ext.modeljson.internal.base.JsonDeserializerBaseVersion
import io.medatarun.ext.modeljson.internal.base.JsonSerializerBaseVersion
import io.medatarun.ext.modeljson.internal.serializers.LocalizedMarkdownSerializer
import io.medatarun.ext.modeljson.internal.serializers.LocalizedTextSerializer
import io.medatarun.ext.modeljson.internal.serializers.valueClassSerializer
import io.medatarun.ext.modeljson.internal.v2.JsonDeserializerV2
import io.medatarun.ext.modeljson.internal.v2.JsonSerializerV2
import io.medatarun.ext.modeljson.internal.v2.ModelJsonV2
import io.medatarun.ext.modeljson.internal.v3.JsonSerializerV3
import io.medatarun.ext.modeljson.internal.v3.ModelJsonV3
import io.medatarun.model.domain.*
import io.medatarun.model.infra.ModelAggregateInMemory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import org.intellij.lang.annotations.Language


internal class ModelJsonConverter(private val prettyPrint: Boolean) {

    val json = Json {
        prettyPrint = this@ModelJsonConverter.prettyPrint
        serializersModule = SerializersModule {
            contextual(ModelKey::class, valueClassSerializer(::ModelKey) { it.value })
            contextual(EntityKey::class, valueClassSerializer(::EntityKey) { it.value })
            contextual(AttributeKey::class, valueClassSerializer(::AttributeKey) { it.value })
            contextual(LocalizedText::class, LocalizedTextSerializer())
            contextual(LocalizedMarkdown::class, LocalizedMarkdownSerializer())
        }
    }

    val serializerBaseVersion = JsonSerializerBaseVersion()
    val serializerV2 = JsonSerializerV2(serializerBaseVersion)
    val serializerV3 = JsonSerializerV3(serializerBaseVersion)

    val deserializerBase = JsonDeserializerBaseVersion()
    val deserializerV2 = JsonDeserializerV2(deserializerBase)


    fun toJsonStringV2(model: ModelAggregate): String {
        val modelJson = serializerV2.toModelJsonV2(model)
        return this.json.encodeToString(ModelJsonV2.serializer(), modelJson)
    }

    fun toJsonObjectV2(model: ModelAggregate): JsonObject {
        val modelJson = serializerV2.toModelJsonV2(model)
        return this.json.encodeToJsonElement(ModelJsonV2.serializer(), modelJson).jsonObject
    }

    fun fromJsonV2(@Language("json") jsonString: String): ModelAggregateInMemory {
        val modelJsonV2 = this.json.decodeFromString(ModelJsonV2.serializer(), jsonString)
        return deserializerV2.fromJsonV2(modelJsonV2)
    }

    fun toJsonStringV3(model: ModelAggregate): String {
        val modelJson = serializerV3.toModelJson(model)
        return this.json.encodeToString(ModelJsonV3.serializer(), modelJson)
    }
}

