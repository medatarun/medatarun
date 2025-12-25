package io.medatarun.httpserver.ui

import io.medatarun.actions.runtime.ActionRegistry
import io.medatarun.model.domain.*
import io.medatarun.runtime.AppRuntime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.util.*
import kotlin.reflect.typeOf

class UI(private val runtime: AppRuntime, private val actionRegistry: ActionRegistry) {

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

    fun modelJson(modelKey: ModelKey, locale: Locale): String {
        val model = runtime.modelQueries.findModelById(modelKey)

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
                        put("id", t.id.value)
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
            put("id", relationship.id.value)
            put("name", relationship.name?.get(locale))
            put("description", relationship.description?.get(locale))
            putJsonArray("roles") {
                relationship.roles.forEach { role ->
                    addJsonObject {
                        put("id", role.id.value)
                        put("name", role.name?.get(locale))
                        put("entityId", role.entityId.value)
                        put("cardinality", role.cardinality.code)
                    }
                }
            }
        }
        return relationshipJson
    }

    private fun entityDefJson(
        e: EntityDef,
        locale: Locale,
        model: Model
    ): JsonObject {
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
                        put("identifierAttribute", e.identifierAttributeKey == attr.id)
                    }
                }
            }
        }
    }



    fun actionRegistryDto(detectLocale: Locale): List<ActionDto> {
        return actionRegistry.findAllActions().map { cmd ->
            ActionDto(
                actionKey = cmd.name,
                groupKey = cmd.group,
                title = cmd.title ?: cmd.name,
                description = cmd.description,
                uiLocation = cmd.uiLocation,
                parameters = cmd.parameters.map { p ->
                    ActionParamDto(
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
data class ActionDto(
    val groupKey: String,
    val actionKey: String,
    val title: String,
    val description: String?,
    val parameters: List<ActionParamDto>,
    val uiLocation: String
)

@Serializable
data class ActionParamDto(
    val name: String,
    val type: String,
    val optional: Boolean,
    val title: String?,
    val description: String?,
    val order: Int
)


