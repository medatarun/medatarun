package io.medatarun.resources.actions

import io.medatarun.model.model.LocalizedText
import io.medatarun.runtime.AppRuntime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.collections.component1
import kotlin.collections.component2

class ModelInspectJsonAction(private val runtime: AppRuntime) {
    private val jsonPretty = Json { prettyPrint = true }
    fun process(): String {
        val root = buildJsonObject {
            put("models", buildJsonArray {
                runtime.modelQueries.findAllModelIds().forEach { modelId ->
                    val model = runtime.modelQueries.findModelById(modelId)
                    add(buildJsonObject {
                        put("id", model.id.value)
                        put("version", model.version.value)
                        put("name", localizedTextToJson(model.name))
                        put("description", localizedTextToJson(model.description))
                        put("types", buildJsonArray {
                            model.types.forEach { type ->
                                add(buildJsonObject {
                                    put("id", type.id.value)
                                    put("name", localizedTextToJson(type.name))
                                    put("description", localizedTextToJson(type.description))
                                })
                            }
                        })
                        put("entities", buildJsonArray {
                            model.entityDefs.forEach { entity ->
                                add(buildJsonObject {
                                    put("id", entity.id.value)
                                    put("name", localizedTextToJson(entity.name))
                                    put("description", localizedTextToJson(entity.description))
                                    put("identifierAttribute", entity.identifierAttributeDefId.value)
                                    put("attributes", buildJsonArray {
                                        entity.attributes.forEach { attribute ->
                                            add(buildJsonObject {
                                                put("id", attribute.id.value)
                                                put("name", localizedTextToJson(attribute.name))
                                                put("description", localizedTextToJson(attribute.description))
                                                put("type", attribute.type.value)
                                                put("optional", attribute.optional)
                                            })
                                        }
                                    })
                                })
                            }
                        })
                    })
                }
            })
        }
        return jsonPretty.encodeToString(root)
    }
    private fun localizedTextToJson(value: LocalizedText?): JsonElement {
        return value?.let { text ->
            buildJsonObject {
                put("values", buildJsonObject {
                    text.all().forEach { (locale, content) ->
                        put(locale, content)
                    }
                })
            }
        } ?: JsonNull
    }
}