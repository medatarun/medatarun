package io.medatarun.httpserver.ui

import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.EntityOrigin
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelOrigin
import io.medatarun.resources.AppResources
import io.medatarun.resources.ResourceRepository
import io.medatarun.runtime.AppRuntime
import kotlinx.serialization.json.*

class UI(private val runtime: AppRuntime) {
    private val resources = AppResources(runtime)
    private val resourceRepository = ResourceRepository(resources)
    fun renderModelListJson(): String {
        val data = runtime.modelQueries.findAllModelSummaries()
        return buildJsonArray {
            data.forEach { m ->
                addJsonObject {
                    put("id", m.id.value)
                    put("name", m.name)
                    put("description", m.description)
                    put("error", m.error)
                    put("countTypes", m.countTypes)
                    put("countEntities", m.countEntities)
                    put("countRelationships", m.countRelationships)
                }
            }
        }.toString()
    }

    fun renderModelJson(modelId: ModelId): String {
        val model = runtime.modelQueries.findModelById(modelId)

        return buildJsonObject {
            put("id", model.id.value)
            put("version", model.version.value)
            put("documentationHome", model.documentationHome?.toExternalForm())
            put("hashtags", JsonArray(model.hashtags.map { JsonPrimitive(it.value) }))
            val origin = model.origin
            put(
                "origin", when {
                    origin is ModelOrigin.Uri -> buildJsonObject {
                        put("type", "uri")
                        put("uri", origin.uri.toString())
                    }

                    else -> buildJsonObject {
                        put("type", "manual")
                    }
                }
            )
            put("name", model.name?.name) // TODO localize
            put("description", model.description?.name) // TODO localize
            putJsonArray("entityDefs") {
                model.entityDefs.forEach { e ->
                    addJsonObject {
                        put("id", e.id.value)
                        put("name", e.name?.name) // TODO localize
                        put("description", e.description?.name) // TODO localize
                    }
                }
            }
            putJsonArray("types") {
                model.types.forEach { t ->
                    addJsonObject {
                        put("id", t.id.value)
                        put("name", t.name?.name)
                        put("description", t.description?.name)
                    }
                }
            }
        }.toString()
    }

    fun renderEntityDefJson(modelId: ModelId, entityDefId: EntityDefId): String {
        val model = runtime.modelQueries.findModelById(modelId)
        val e = model.findEntityDef(entityDefId)
        val id = e.id.value
        val name = e.name?.name
        val description = e.description?.name
        val origin = e.origin
        val documentationHome = e.documentationHome
        return buildJsonObject {
            put("id", id)
            put("name", name)
            put("description", description)
            put("documentationHome", documentationHome?.toExternalForm())
            put("hashtags", JsonArray(e.hashtags.map { JsonPrimitive(it.value) }))
            putJsonObject("model") {
                put("id", model.id.value)
                put("name", model.name?.name)
            }
            put(
                "origin", when {
                    origin is EntityOrigin.Uri -> buildJsonObject {
                        put("type", "uri")
                        put("uri", origin.uri.toString())
                    }

                    else -> buildJsonObject {
                        put("type", "manual")
                    }
                }
            )
            putJsonArray("attributes") {
                e.attributes.forEach { attr ->
                    addJsonObject {
                        put("id", attr.id.value)
                        put("name", attr.name?.name)
                        put("description", attr.description?.name)
                        put("type", attr.type.value)
                        put("optional", attr.optional)
                        put("identifierAttribute", e.identifierAttributeDefId == attr.id)
                    }
                }
            }
        }.toString()
    }


}

