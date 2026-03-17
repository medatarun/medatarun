package io.medatarun.model.actions.search

import io.medatarun.model.domain.*
import io.medatarun.model.domain.search.SearchResults
import kotlinx.serialization.json.*

object ModelSearchDtoAdapters {

    fun toModelSearchResults(result: SearchResults): JsonObject {
        return buildJsonObject {
            putJsonArray("items") {
                for (item in result.items) {
                    addJsonObject {
                        put("id", item.id)
                        put("location", createLocation(item.location))

                    }
                }
            }
        }
    }

    fun createLocation(location: DomainLocation): JsonObject {
        return buildJsonObject {
            put("objectType", location.objectType)
            addLocation(location)
        }
    }
}

private fun JsonObjectBuilder.addLocation(location: DomainLocation) {
    when (location) {
        is ModelLocation -> {
            put("modelId", location.id.asString())
            put("modelKey", location.key.value)
            put("modelLabel", location.label)
        }

        is TypeLocation -> {
            addLocation(location.model)
            put("typeId", location.id.asString())
            put("typeKey", location.key.value)
            put("typeLabel", location.label)
        }

        is EntityLocation -> {
            addLocation(location.model)
            put("entityId", location.id.asString())
            put("entityKey", location.key.value)
            put("entityLabel", location.label)
        }

        is EntityAttributeLocation -> {
            addLocation(location.entity.model)
            addLocation(location.entity)
            put("entityAttributeId", location.id.asString())
            put("entityAttributeKey", location.key.value)
            put("entityAttributeLabel", location.label)
        }

        is RelationshipLocation -> {
            addLocation(location.model)
            put("relationshipId", location.id.asString())
            put("relationshipKey", location.key.value)
            put("relationshipLabel", location.label)
        }

        is RelationshipAttributeLocation -> {
            addLocation(location.relationship.model)
            addLocation(location.relationship)
            put("relationshipAttributeId", location.id.asString())
            put("relationshipAttributeKey", location.key.value)
            put("relationshipAttributeLabel", location.label)
        }
    }
}
