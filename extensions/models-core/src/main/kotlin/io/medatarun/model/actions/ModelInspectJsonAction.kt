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
                        put("name", model.name?.name)
                        put("description", model.description?.name)
                        put("types", buildJsonArray {
                            model.types.forEach { type ->
                                add(buildJsonObject {
                                    put("id", type.key.value)
                                    put("name", type.name?.name)
                                    put("description", type.description?.name)
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
                                    put("name", entity.name?.name)
                                    put("description", entity.description?.name)
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
                                    put("name", relationship.name?.name)
                                    put("description", relationship.description?.name)
                                    put("attributes", toAttributesJson(model, relationship.ref))
                                    putJsonArray("roles") {
                                        relationship.roles.forEach { role ->
                                            addJsonObject {
                                                put("id", role.key.value)
                                                put("name", role.name?.name)
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
                                    put("name", bk.name?.name)
                                    put("description", bk.description?.name)
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
                    put("name", attribute.name?.name)
                    put("description", attribute.description?.name)
                    put("type", type.key.value)
                    put("optional", attribute.optional)
                })
            }

        }
    }

}

