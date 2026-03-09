package io.medatarun.httpserver.ui

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode
import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.platform.kernel.PlatformRuntime
import io.medatarun.platform.kernel.getService
import kotlinx.serialization.json.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class UI(runtime: PlatformRuntime) {

    val modelQueries = runtime.services.getService<ModelQueries>()

    fun modelListJson(locale: Locale): String {
        try {
            val data = modelQueries.findAllModelSummaries(locale)
            return buildJsonArray {
                data.forEach { m ->
                    addJsonObject {
                        put("id", m.id.asString())
                        put("key", m.key.asString())
                        put("name", m.name)
                        put("description", m.description)
                        put("authority", m.authority.code)
                        put("error", m.error)
                        put("countTypes", m.countTypes)
                        put("countEntities", m.countEntities)
                        put("countRelationships", m.countRelationships)
                    }
                }
            }.toString()
        } catch(e: Exception) {
            logger.error("Impossible to read models", e)
            throw MedatarunException("Could not get list of models", StatusCode.INTERNAL_SERVER_ERROR)
        }
    }

    fun modelJson(modelId: ModelId, locale: Locale): String {
        val model = modelQueries.findModelById(modelId)

        return buildJsonObject {
            put("id", model.id.asString())
            put("key", model.key.asString())
            put("name", model.name?.get(locale))
            put("version", model.version.value)
            put("authority", model.authority.code)
            put("documentationHome", model.documentationHome?.toExternalForm())
            put("tags", JsonArray(model.tags.map { JsonPrimitive(it.value.toString()) }))
            put("description", model.description?.get(locale))
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


            putJsonArray("entities") {
                model.entities.forEach { e ->
                    add(entityJson(e, locale, model))
                }
            }
            putJsonArray("relationships", {
                model.relationships.forEach { relationship ->
                    val relationshipJson = toRelationshipJson(model, relationship, locale)
                    add(relationshipJson)
                }
            })

            putJsonArray("types") {
                model.types.forEach { t ->
                    addJsonObject {
                        put("id", t.id.value.toString())
                        put("key", t.key.value)
                        put("name", t.name?.get(locale))
                        put("description", t.description?.get(locale))
                    }
                }
            }
        }.toString()
    }

    private fun toRelationshipJson(
        modelAggregate: ModelAggregate,
        relationship: Relationship,
        locale: Locale
    ): JsonObject {
        val relationshipJson = buildJsonObject {
            put("id", relationship.id.asString())
            put("key", relationship.key.asString())
            put("name", relationship.name?.get(locale))
            put("description", relationship.description?.get(locale))
            put("tags", JsonArray(relationship.tags.map { JsonPrimitive(it.value.toString()) }))
            putJsonArray("roles") {
                relationship.roles.forEach { role ->
                    addJsonObject {
                        put("id", role.id.asString())
                        put("key", role.key.asString())
                        put("name", role.name?.get(locale))
                        put("entityId", role.entityId.value.toString())
                        put("cardinality", role.cardinality.code)
                    }
                }
            }
            putJsonArray("attributes") { modelAggregate.findRelationshipAttributes(relationship.ref).forEach { attr ->
                addJsonObject {
                    put("id", attr.id.asString())
                    put("key", attr.key.asString())
                    put("name", attr.name?.get(locale))
                    put("description", attr.description?.get(locale))
                    put("type", attr.typeId.value.toString())
                    put("optional", attr.optional)
                    put("identifierAttribute", false)
                    putJsonArray("tags") {
                        attr.tags.forEach { add(it.value.toString()) }
                    }
                }
            } }
        }
        return relationshipJson
    }

    private fun entityJson(
        e: Entity,
        locale: Locale,
        model: ModelAggregate
    ): JsonObject {
        val name = e.name?.get(locale)
        val description = e.description?.get(locale)
        val origin = e.origin
        val documentationHome = e.documentationHome
        return buildJsonObject {
            put("id", e.id.asString())
            put("key", e.key.asString())
            put("name", name)
            put("description", description)
            put("documentationHome", documentationHome?.toExternalForm())
            putJsonArray("tags") {
                e.tags.forEach { add(it.value.toString()) }
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
                val attributes = model.findEntityAttributes(e.ref)
                attributes.forEach { attr ->
                    addJsonObject {
                        put("id", attr.id.asString())
                        put("key", attr.key.asString())
                        put("name", attr.name?.get(locale))
                        put("description", attr.description?.get(locale))
                        put("type", attr.typeId.value.toString())
                        put("optional", attr.optional)
                        put("identifierAttribute", e.identifierAttributeId == attr.id)
                        putJsonArray("tags") {
                            attr.tags.forEach { add(it.value.toString()) }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(UI::class.java)
    }
}
