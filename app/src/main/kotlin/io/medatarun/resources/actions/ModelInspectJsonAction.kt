package io.medatarun.resources.actions

import io.medatarun.model.model.AttributeDef
import io.medatarun.model.model.LocalizedText
import io.medatarun.resources.ActionCtx
import kotlinx.serialization.json.*

class ModelInspectJsonAction(private val actionCtx: ActionCtx) {
    private val jsonPretty = Json { prettyPrint = true }
    fun process(): String {
        val root = buildJsonObject {
            put("models", buildJsonArray {
                actionCtx.modelQueries.findAllModelIds().forEach { modelId ->
                    val model = actionCtx.modelQueries.findModelById(modelId)
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
                                    put("attributes", toAttributesJson(entity.attributes))
                                })
                            }
                        })
                        put("relationships", buildJsonArray {
                            model.relationshipDefs.forEach { relationship ->
                                addJsonObject {
                                    put("id", relationship.id.value)
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
                put("id", attribute.id.value)
                put("name", localizedTextToJson(attribute.name))
                put("description", localizedTextToJson(attribute.description))
                put("type", attribute.type.value)
                put("optional", attribute.optional)
            })
        }

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