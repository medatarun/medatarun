package io.medatarun.httpserver.ui

import io.medatarun.actions.runtime.ActionRegistry
import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.platform.kernel.getService
import io.medatarun.runtime.AppRuntime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.util.*

class UI(runtime: AppRuntime, private val actionRegistry: ActionRegistry) {

    val modelQueries = runtime.services.getService<ModelQueries>()

    data class ModelSummaryDto(
        val id: String,
        val name: String?,
        val description: String?,
        val error: String?,
        val countTypes: Int,
        val countEntities: Int,
        val countRelationships: Int
    )

    fun modelListJson(locale: Locale): String {
        val data = modelQueries.findAllModelSummaries(locale)
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

    fun modelJson(modelKey: ModelKey, locale: Locale): String {
        val model = modelQueries.findModelById(modelKey)

        return buildJsonObject {
            put("id", model.key.value)
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
                    add(entityDefJson(e, locale, model))
                }
            }
            putJsonArray("relationshipDefs", {
                model.relationshipDefs.forEach { relationship ->
                    val relationshipJson = toRelationshipJson(relationship, locale)
                    add(relationshipJson)
                }
            })

            putJsonArray("types") {
                model.types.forEach { t ->
                    addJsonObject {
                        put("id", t.key.value)
                        put("name", t.name?.get(locale))
                        put("description", t.description?.get(locale))
                    }
                }
            }
        }.toString()
    }

    private fun toRelationshipJson(
        relationship: RelationshipDef,
        locale: Locale
    ): JsonObject {
        val relationshipJson = buildJsonObject {
            put("id", relationship.key.value)
            put("name", relationship.name?.get(locale))
            put("description", relationship.description?.get(locale))
            put("hashtags", JsonArray(relationship.hashtags.map { JsonPrimitive(it.value) }))
            putJsonArray("roles") {
                relationship.roles.forEach { role ->
                    addJsonObject {
                        put("id", role.key.value)
                        put("name", role.name?.get(locale))
                        put("entityId", role.entityId.value)
                        put("cardinality", role.cardinality.code)
                    }
                }
            }
            putJsonArray("attributes") { relationship.attributes.forEach { attr ->
                addJsonObject {
                    put("id", attr.key.value)
                    put("name", attr.name?.get(locale))
                    put("description", attr.description?.get(locale))
                    put("type", attr.type.value)
                    put("optional", attr.optional)
                    put("identifierAttribute", false)
                    putJsonArray("hashtags") {
                        attr.hashtags.forEach { add(it.value) }
                    }
                }
            } }
        }
        return relationshipJson
    }

    private fun entityDefJson(
        e: EntityDef,
        locale: Locale,
        model: Model
    ): JsonObject {
        val id = e.key.value
        val name = e.name?.get(locale)
        val description = e.description?.get(locale)
        val origin = e.origin
        val documentationHome = e.documentationHome
        return buildJsonObject {
            put("id", id)
            put("name", name)
            put("description", description)
            put("documentationHome", documentationHome?.toExternalForm())
            putJsonArray("hashtags") {
                e.hashtags.forEach { add(it.value) }
            }
            putJsonObject("model") {
                put("id", model.key.value)
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
                        put("id", attr.key.value)
                        put("name", attr.name?.get(locale))
                        put("description", attr.description?.get(locale))
                        put("type", attr.type.value)
                        put("optional", attr.optional)
                        put("identifierAttribute", e.identifierAttributeKey == attr.key)
                        putJsonArray("hashtags") {
                            attr.hashtags.forEach { add(it.value) }
                        }
                    }
                }
            }
        }
    }



    fun actionRegistryDto(detectLocale: Locale): List<ActionDescriptorDto> {
        return actionRegistry.findAllActions().map { cmd ->
            ActionDescriptorDto(
                actionKey = cmd.key,
                groupKey = cmd.group,
                title = cmd.title ?: cmd.key,
                description = cmd.description,
                uiLocations = cmd.uiLocations,
                parameters = cmd.parameters.map { p ->
                    ActionParamDescriptorDto(
                        name = p.name,
                        type = p.multiplatformType,
                        optional = p.optional,
                        title = p.title,
                        description = p.description,
                        order = p.order
                    )
                }
            )
        }
    }
}


@Serializable
data class ActionDescriptorDto(
    val groupKey: String,
    val actionKey: String,
    val title: String,
    val description: String?,
    val parameters: List<ActionParamDescriptorDto>,
    val uiLocations: Set<String>
)

@Serializable
data class ActionParamDescriptorDto(
    val name: String,
    val type: String,
    val optional: Boolean,
    val title: String?,
    val description: String?,
    val order: Int
)


