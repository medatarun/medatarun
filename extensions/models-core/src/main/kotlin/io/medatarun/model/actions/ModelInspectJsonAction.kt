package io.medatarun.model.actions

import io.medatarun.model.domain.AttributeDef
import io.medatarun.model.domain.LocalizedTextBase
import io.medatarun.model.ports.exposed.ModelQueries
import kotlinx.serialization.json.*

class ModelInspectJsonAction(private val modelQueries: ModelQueries) {
    private val jsonPretty = Json { prettyPrint = true }
    fun process(): String {
        val root = buildJsonObject {
            put("models", buildJsonArray {
                modelQueries.findAllModelIds().forEach { modelId ->
                    val model = modelQueries.findModelById(modelId)
                    add(buildJsonObject {
                        put("id", model.key.value)
                        put("version", model.version.value)
                        put("name", localizedTextToJson(model.name))
                        put("description", localizedTextToJson(model.description))
                        put("types", buildJsonArray {
                            model.types.forEach { type ->
                                add(buildJsonObject {
                                    put("id", type.key.value)
                                    put("name", localizedTextToJson(type.name))
                                    put("description", localizedTextToJson(type.description))
                                })
                            }
                        })
                        put("entities", buildJsonArray {
                            model.entityDefs.forEach { entity ->
                                add(buildJsonObject {
                                    put("id", entity.key.value)
                                    put("name", localizedTextToJson(entity.name))
                                    put("description", localizedTextToJson(entity.description))
                                    put("identifierAttribute", entity.identifierAttributeKey.value)
                                    put("attributes", toAttributesJson(entity.attributes))
                                })
                            }
                        })
                        put("relationships", buildJsonArray {
                            model.relationshipDefs.forEach { relationship ->
                                addJsonObject {
                                    put("id", relationship.key.value)
                                    put("name", localizedTextToJson(relationship.name))
                                    put("description", localizedTextToJson(relationship.description))
                                    put("attributes", toAttributesJson(relationship.attributes))
                                    putJsonArray("roles") {
                                        relationship.roles.forEach { role ->
                                            addJsonObject {
                                                put("id", role.id.value)
                                                put("name", localizedTextToJson(role.name))
                                                put("entityId", role.entityId.value)
                                                put("cardinality", role.cardinality.code)
                                            }
                                        }
                                    }
                                }
                            }
                        })
                    })
                }
            })
        }
        return jsonPretty.encodeToString(root)
    }

    private fun toAttributesJson(attributes: List<AttributeDef>): JsonArray = buildJsonArray {

        attributes.forEach { attribute ->
            add(buildJsonObject {
                put("id", attribute.key.value)
                put("name", localizedTextToJson(attribute.name))
                put("description", localizedTextToJson(attribute.description))
                put("type", attribute.type.value)
                put("optional", attribute.optional)
            })
        }

    }

    private fun localizedTextToJson(value: LocalizedTextBase?): JsonElement {
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