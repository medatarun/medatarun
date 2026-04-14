package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.ModelQueries
import kotlinx.serialization.json.*

class ModelInspectJsonAction(private val modelQueries: ModelQueries) {
    private val jsonPretty = Json { prettyPrint = true }
    fun process(): String {
        val root = buildJsonObject {
            put("models", buildJsonArray {
                modelQueries.findAllModelIds().forEach { modelId ->
                    val model = modelQueries.findModelAggregateById(modelId)
                    add(buildJsonObject {
                        put("id", model.key.value)
                        put("version", model.version.value)
                        put("authority", model.authority.code)
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
                            model.entities.forEach { entity ->
                                val pkList: List<String> = model.findEntityPrimaryKeyOptional(entity.id)
                                    ?.participants
                                    ?.map { it.attributeId.asString() }
                                    ?: emptyList()
                                add(buildJsonObject {
                                    put("id", entity.key.value)
                                    put("name", localizedTextToJson(entity.name))
                                    put("description", localizedTextToJson(entity.description))
                                    put("attributes", toAttributesJson(model, entity.ref))
                                    putJsonArray("primaryKey") {
                                        for (p in pkList) add(p)
                                    }
                                })
                            }
                        })
                        put("relationships", buildJsonArray {
                            model.relationships.forEach { relationship ->
                                addJsonObject {
                                    put("id", relationship.key.value)
                                    put("name", localizedTextToJson(relationship.name))
                                    put("description", localizedTextToJson(relationship.description))
                                    put("attributes", toAttributesJson(model, relationship.ref))
                                    putJsonArray("roles") {
                                        relationship.roles.forEach { role ->
                                            addJsonObject {
                                                put("id", role.key.value)
                                                put("name", localizedTextToJson(role.name))
                                                put("entityId", model.findEntity(role.entityId).id.value.toString())
                                                put("cardinality", role.cardinality.code)
                                            }
                                        }
                                    }
                                }
                            }
                        })
                        put("businessKeys", buildJsonArray {
                            for (bk in model.businessKeys) {
                                addJsonObject {
                                    put("id", bk.id.asString())
                                    put("entityId", bk.entityId.asString())
                                    put("key", bk.key.asString())
                                    put("name", localizedTextToJson(bk.name))
                                    put("description", localizedTextToJson(bk.description))
                                    putJsonArray("participants", {
                                        for (p in bk.participants.sortedBy { it.position }) {
                                            add(p.attributeId.asString())
                                        }
                                    })
                                }
                            }
                        })
                    })
                }
            })
        }
        return jsonPretty.encodeToString(root)
    }

    private fun toAttributesJson(model: ModelAggregate, entityRef: EntityRef): JsonArray {
        val attributes = model.findEntityAttributes(entityRef)
        return toAttributesJson(attributes, model)
    }

    private fun toAttributesJson(model: ModelAggregate, relationshipRef: RelationshipRef): JsonArray {
        val attributes = model.findRelationshipAttributes(relationshipRef)
        return toAttributesJson(attributes, model)
    }

    private fun toAttributesJson(
        attributes: List<Attribute>,
        model: ModelAggregate
    ): JsonArray {
        return buildJsonArray {
            attributes.forEach { attribute ->
                val type = model.findType(TypeRef.ById(attribute.typeId))
                add(buildJsonObject {
                    put("id", attribute.key.value)
                    put("name", localizedTextToJson(attribute.name))
                    put("description", localizedTextToJson(attribute.description))
                    put("type", type.key.value)
                    put("optional", attribute.optional)
                })
            }

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
