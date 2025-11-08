package io.medatarun.httpserver.ui

import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.EntityOrigin
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelOrigin
import io.medatarun.runtime.AppRuntime
import kotlinx.serialization.json.*
import java.util.Locale

class UI(private val runtime: AppRuntime) {

    fun modelListJson(locale: Locale): String {
        val data = runtime.modelQueries.findAllModelSummaries(locale)
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

    fun modelJson(modelId: ModelId, locale: Locale): String {
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
            put("name", model.name?.get(locale))
            put("description", model.description?.get(locale))
            putJsonArray("entityDefs") {
                model.entityDefs.forEach { e ->
                    addJsonObject {
                        put("id", e.id.value)
                        put("name", e.name?.get(locale))
                        put("description", e.description?.get(locale))
                    }
                }
            }
            putJsonArray("types") {
                model.types.forEach { t ->
                    addJsonObject {
                        put("id", t.id.value)
                        put("name", t.name?.get(locale))
                        put("description", t.description?.get(locale))
                    }
                }
            }
        }.toString()
    }

    fun entityDefJson(modelId: ModelId, entityDefId: EntityDefId, locale: Locale): String {
        val model = runtime.modelQueries.findModelById(modelId)
        val e = model.findEntityDef(entityDefId)
        val id = e.id.value
        val name = e.name?.get(locale)
        val description = e.description?.get(locale)
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
                put("name", model.name?.get(locale))
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
                        put("name", attr.name?.get(locale))
                        put("description", attr.description?.get(locale))
                        put("type", attr.type.value)
                        put("optional", attr.optional)
                        put("identifierAttribute", e.identifierAttributeDefId == attr.id)
                    }
                }
            }
        }.toString()
    }


}

