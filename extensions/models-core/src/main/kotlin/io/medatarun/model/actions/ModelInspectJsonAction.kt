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
                            model.entities.forEach { entity ->
                                val identifierAttribute = model.findEntityAttribute(
                                    EntityRef.ById(entity.id),
                                    EntityAttributeRef.ById(entity.identifierAttributeId)
                                )
                                add(buildJsonObject {
                                    put("id", entity.key.value)
                                    put("name", localizedTextToJson(entity.name))
                                    put("description", localizedTextToJson(entity.description))
                                    put("identifierAttribute", identifierAttribute.key.value)
                                    put("attributes", toAttributesJson(model, entity.ref))
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
                    })
                }
            })
        }
        return jsonPretty.encodeToString(root)
    }

    private fun toAttributesJson(model: Model, entityRef: EntityRef): JsonArray {
        val attributes = model.findEntityAttributes(entityRef)
        return toAttributesJson(attributes, model)
    }
    private fun toAttributesJson(model: Model, relationshipRef: RelationshipRef): JsonArray {
        val attributes = model.findRelationshipAttributes(relationshipRef)
        return toAttributesJson(attributes, model)
    }

    private fun toAttributesJson(
        attributes: List<Attribute>,
        model: Model
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