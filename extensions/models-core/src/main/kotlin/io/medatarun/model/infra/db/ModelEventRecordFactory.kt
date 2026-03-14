package io.medatarun.model.infra.db

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.records.ModelEventRecord
import io.medatarun.model.ports.exposed.ModelTypeInitializer
import io.medatarun.model.ports.needs.ModelRepoCmd
import io.medatarun.model.ports.needs.ModelRepoCmdEnveloppe
import io.medatarun.model.ports.needs.StoreModelAggregateEntity
import io.medatarun.model.ports.needs.StoreModelAggregateEntityAttribute
import io.medatarun.model.ports.needs.StoreModelAggregateModel
import io.medatarun.model.ports.needs.StoreModelAggregateRelationship
import io.medatarun.model.ports.needs.StoreModelAggregateRelationshipAttribute
import io.medatarun.model.ports.needs.StoreModelAggregateRelationshipRole
import io.medatarun.model.ports.needs.StoreModelAggregateType
import kotlinx.serialization.json.*

/**
 * Transforms repository command envelopes into stable event records.
 *
 * The payload is written with explicit field names so the event contract does not
 * depend on Kotlin class or property names.
 */
class ModelEventRecordFactory {

    fun create(cmdEnv: ModelRepoCmdEnveloppe, streamRevision: Int, createdAt: java.time.Instant): ModelEventRecord {
        val event = toEvent(cmdEnv.cmd)
        return ModelEventRecord(
            id = UuidUtils.generateV7().toString(),
            modelId = extractModelId(cmdEnv.cmd),
            streamRevision = streamRevision,
            eventType = event.type,
            eventVersion = EVENT_VERSION,
            modelVersion = event.modelVersion,
            actorId = cmdEnv.principalId,
            actionId = cmdEnv.actionId.value.toString(),
            createdAt = createdAt,
            payload = event.payload.toString()
        )
    }

    private fun extractModelId(cmd: ModelRepoCmd): ModelId {
        return when (cmd) {
            is ModelRepoCmd.CreateModel -> cmd.model.id
            is ModelRepoCmd.StoreModelAggregate -> cmd.model.id
            else -> (cmd as? io.medatarun.model.ports.needs.ModelRepoCmdOnModel)?.modelId
                ?: throw IllegalArgumentException("Unsupported command type: ${cmd::class.qualifiedName}")
        }
    }

    private fun toEvent(cmd: ModelRepoCmd): EventData {
        return when (cmd) {
            is ModelRepoCmd.StoreModelAggregate -> event("model_aggregate_stored", payload = modelAggregatePayload(cmd))
            is ModelRepoCmd.CreateModel -> event("model_created", payload = modelPayload(cmd.model))
            is ModelRepoCmd.UpdateModelName -> event("model_name_updated", payload = buildJsonObject {
                putNullable("name", json(cmd.name))
            })
            is ModelRepoCmd.UpdateModelKey -> event("model_key_updated", payload = buildJsonObject {
                put("key", cmd.key.value)
            })
            is ModelRepoCmd.UpdateModelDescription -> event("model_description_updated", payload = buildJsonObject {
                putNullable("description", json(cmd.description))
            })
            is ModelRepoCmd.UpdateModelAuthority -> event("model_authority_updated", payload = buildJsonObject {
                put("authority", cmd.authority.name)
            })
            is ModelRepoCmd.UpdateModelVersion -> event("model_version_updated", payload = buildJsonObject {
                put("version", cmd.version.value)
            })
            is ModelRepoCmd.UpdateModelDocumentationHome -> event("model_documentation_home_updated", payload = buildJsonObject {
                putNullable("documentation_home", cmd.url?.toExternalForm()?.let { JsonPrimitive(it) })
            })
            is ModelRepoCmd.UpdateModelTagAdd -> event("model_tag_added", payload = buildJsonObject {
                put("tag_id", cmd.tagId.value.toString())
            })
            is ModelRepoCmd.UpdateModelTagDelete -> event("model_tag_deleted", payload = buildJsonObject {
                put("tag_id", cmd.tagId.value.toString())
            })
            is ModelRepoCmd.DeleteModel -> event("model_deleted", payload = JsonObject(emptyMap()))
            is ModelRepoCmd.CreateType -> event("type_created", payload = buildJsonObject {
                put("initializer", modelTypeInitializerPayload(cmd.initializer))
            })
            is ModelRepoCmd.UpdateTypeKey -> event("type_key_updated", payload = buildJsonObject {
                put("type_id", cmd.typeId.value.toString())
                put("key", cmd.value.value)
            })
            is ModelRepoCmd.UpdateTypeName -> event("type_name_updated", payload = buildJsonObject {
                put("type_id", cmd.typeId.value.toString())
                putNullable("name", json(cmd.value))
            })
            is ModelRepoCmd.UpdateTypeDescription -> event("type_description_updated", payload = buildJsonObject {
                put("type_id", cmd.typeId.value.toString())
                putNullable("description", json(cmd.value))
            })
            is ModelRepoCmd.DeleteType -> event("type_deleted", payload = buildJsonObject {
                put("type_id", cmd.typeId.value.toString())
            })
            is ModelRepoCmd.CreateEntity -> event("entity_created", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("key", cmd.key.value)
                putNullable("name", json(cmd.name))
                putNullable("description", json(cmd.description))
                putNullable("documentation_home", cmd.documentationHome?.toExternalForm()?.let { JsonPrimitive(it) })
                put("origin", entityOriginPayload(cmd.origin))
                put("identity_attribute", buildJsonObject {
                    put("attribute_id", cmd.identityAttributeId.value.toString())
                    put("key", cmd.identityAttributeKey.value)
                    put("type_id", cmd.identityAttributeTypeId.value.toString())
                    putNullable("name", json(cmd.identityAttributeName))
                    putNullable("description", json(cmd.identityAttributeDescription))
                    put("optional", cmd.identityAttributeIdOptional)
                })
            })
            is ModelRepoCmd.UpdateEntityKey -> event("entity_key_updated", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("key", cmd.value.value)
            })
            is ModelRepoCmd.UpdateEntityName -> event("entity_name_updated", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                putNullable("name", json(cmd.value))
            })
            is ModelRepoCmd.UpdateEntityDescription -> event("entity_description_updated", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                putNullable("description", json(cmd.value))
            })
            is ModelRepoCmd.UpdateEntityIdentifierAttribute -> event("entity_identifier_attribute_updated", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("attribute_id", cmd.value.value.toString())
            })
            is ModelRepoCmd.UpdateEntityDocumentationHome -> event("entity_documentation_home_updated", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                putNullable("documentation_home", cmd.value?.toExternalForm()?.let { JsonPrimitive(it) })
            })
            is ModelRepoCmd.UpdateEntityTagAdd -> event("entity_tag_added", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("tag_id", cmd.tagId.value.toString())
            })
            is ModelRepoCmd.UpdateEntityTagDelete -> event("entity_tag_deleted", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("tag_id", cmd.tagId.value.toString())
            })
            is ModelRepoCmd.DeleteEntity -> event("entity_deleted", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
            })
            is ModelRepoCmd.CreateEntityAttribute -> event("entity_attribute_created", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                put("key", cmd.key.value)
                putNullable("name", json(cmd.name))
                putNullable("description", json(cmd.description))
                put("type_id", cmd.typeId.value.toString())
                put("optional", cmd.optional)
            })
            is ModelRepoCmd.DeleteEntityAttribute -> event("entity_attribute_deleted", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
            })
            is ModelRepoCmd.UpdateEntityAttributeKey -> event("entity_attribute_key_updated", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                put("key", cmd.value.value)
            })
            is ModelRepoCmd.UpdateEntityAttributeName -> event("entity_attribute_name_updated", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                putNullable("name", json(cmd.value))
            })
            is ModelRepoCmd.UpdateEntityAttributeDescription -> event("entity_attribute_description_updated", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                putNullable("description", json(cmd.value))
            })
            is ModelRepoCmd.UpdateEntityAttributeType -> event("entity_attribute_type_updated", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                put("type_id", cmd.value.value.toString())
            })
            is ModelRepoCmd.UpdateEntityAttributeOptional -> event("entity_attribute_optional_updated", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                put("optional", cmd.value)
            })
            is ModelRepoCmd.UpdateEntityAttributeTagAdd -> event("entity_attribute_tag_added", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                put("tag_id", cmd.tagId.value.toString())
            })
            is ModelRepoCmd.UpdateEntityAttributeTagDelete -> event("entity_attribute_tag_deleted", payload = buildJsonObject {
                put("entity_id", cmd.entityId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                put("tag_id", cmd.tagId.value.toString())
            })
            is ModelRepoCmd.CreateRelationship -> event("relationship_created", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("key", cmd.key.value)
                putNullable("name", json(cmd.name))
                putNullable("description", json(cmd.description))
                putJsonArray("roles") {
                    cmd.roles.forEach { role ->
                        add(buildJsonObject {
                            put("role_id", role.id.value.toString())
                            put("key", role.key.value)
                            put("entity_id", role.entityId.value.toString())
                            putNullable("name", json(role.name))
                            put("cardinality", role.cardinality.code)
                        })
                    }
                }
            })
            is ModelRepoCmd.UpdateRelationshipKey -> event("relationship_key_updated", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("key", cmd.value.value)
            })
            is ModelRepoCmd.UpdateRelationshipName -> event("relationship_name_updated", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                putNullable("name", json(cmd.value))
            })
            is ModelRepoCmd.UpdateRelationshipDescription -> event("relationship_description_updated", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                putNullable("description", json(cmd.value))
            })
            is ModelRepoCmd.CreateRelationshipRole -> event("relationship_role_created", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("role_id", cmd.relationshipRoleId.value.toString())
                put("key", cmd.key.value)
                put("entity_id", cmd.entityId.value.toString())
                putNullable("name", json(cmd.name))
                put("cardinality", cmd.cardinality.code)
            })
            is ModelRepoCmd.UpdateRelationshipRoleKey -> event("relationship_role_key_updated", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("role_id", cmd.relationshipRoleId.value.toString())
                put("key", cmd.value.value)
            })
            is ModelRepoCmd.UpdateRelationshipRoleName -> event("relationship_role_name_updated", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("role_id", cmd.relationshipRoleId.value.toString())
                putNullable("name", json(cmd.value))
            })
            is ModelRepoCmd.UpdateRelationshipRoleEntity -> event("relationship_role_entity_updated", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("role_id", cmd.relationshipRoleId.value.toString())
                put("entity_id", cmd.value.value.toString())
            })
            is ModelRepoCmd.UpdateRelationshipRoleCardinality -> event("relationship_role_cardinality_updated", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("role_id", cmd.relationshipRoleId.value.toString())
                put("cardinality", cmd.value.code)
            })
            is ModelRepoCmd.UpdateRelationshipTagAdd -> event("relationship_tag_added", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("tag_id", cmd.tagId.value.toString())
            })
            is ModelRepoCmd.UpdateRelationshipTagDelete -> event("relationship_tag_deleted", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("tag_id", cmd.tagId.value.toString())
            })
            is ModelRepoCmd.DeleteRelationship -> event("relationship_deleted", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
            })
            is ModelRepoCmd.DeleteRelationshipRole -> event("relationship_role_deleted", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("role_id", cmd.relationshipRoleId.value.toString())
            })
            is ModelRepoCmd.CreateRelationshipAttribute -> event("relationship_attribute_created", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                put("key", cmd.key.value)
                putNullable("name", json(cmd.name))
                putNullable("description", json(cmd.description))
                put("type_id", cmd.typeId.value.toString())
                put("optional", cmd.optional)
            })
            is ModelRepoCmd.UpdateRelationshipAttributeKey -> event("relationship_attribute_key_updated", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                put("key", cmd.value.value)
            })
            is ModelRepoCmd.UpdateRelationshipAttributeName -> event("relationship_attribute_name_updated", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                putNullable("name", json(cmd.value))
            })
            is ModelRepoCmd.UpdateRelationshipAttributeDescription -> event("relationship_attribute_description_updated", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                putNullable("description", json(cmd.value))
            })
            is ModelRepoCmd.UpdateRelationshipAttributeType -> event("relationship_attribute_type_updated", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                put("type_id", cmd.value.value.toString())
            })
            is ModelRepoCmd.UpdateRelationshipAttributeOptional -> event("relationship_attribute_optional_updated", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                put("optional", cmd.value)
            })
            is ModelRepoCmd.UpdateRelationshipAttributeTagAdd -> event("relationship_attribute_tag_added", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                put("tag_id", cmd.tagId.value.toString())
            })
            is ModelRepoCmd.UpdateRelationshipAttributeTagDelete -> event("relationship_attribute_tag_deleted", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
                put("tag_id", cmd.tagId.value.toString())
            })
            is ModelRepoCmd.DeleteRelationshipAttribute -> event("relationship_attribute_deleted", payload = buildJsonObject {
                put("relationship_id", cmd.relationshipId.value.toString())
                put("attribute_id", cmd.attributeId.value.toString())
            })
        }
    }

    private fun modelAggregatePayload(model: ModelRepoCmd.StoreModelAggregate): JsonObject {
        return buildJsonObject {
            put("model", modelAggregateModelPayload(model.model))
            putJsonArray("types") {
                model.types.sortedBy { it.key.value }.forEach { add(modelAggregateTypePayload(it)) }
            }
            putJsonArray("entities") {
                model.entities.sortedBy { it.key.value }.forEach { entity ->
                    add(
                        modelAggregateEntityPayload(
                            entity,
                            model.entityAttributes.filter { attribute -> attribute.entityId == entity.id }
                        )
                    )
                }
            }
            putJsonArray("relationships") {
                model.relationships.sortedBy { it.key.value }.forEach { relationship ->
                    add(
                        modelAggregateRelationshipPayload(
                            relationship,
                            model.relationshipAttributes.filter { attribute -> attribute.relationshipId == relationship.id }
                        )
                    )
                }
            }
        }
    }

    private fun modelAggregateModelPayload(model: StoreModelAggregateModel): JsonObject {
        return buildJsonObject {
            put("model_id", model.id.value.toString())
            put("key", model.key.value)
            putNullable("name", json(model.name))
            putNullable("description", json(model.description))
            put("version", model.version.value)
            put("origin", modelOriginPayload(model.origin))
            put("authority", model.authority.name)
            putNullable("documentation_home", model.documentationHome?.toExternalForm()?.let { JsonPrimitive(it) })
        }
    }

    private fun modelPayload(model: Model): JsonObject {
        return buildJsonObject {
            put("model_id", model.id.value.toString())
            put("key", model.key.value)
            putNullable("name", json(model.name))
            putNullable("description", json(model.description))
            put("version", model.version.value)
            put("origin", modelOriginPayload(model.origin))
            put("authority", model.authority.name)
            putNullable("documentation_home", model.documentationHome?.toExternalForm()?.let { JsonPrimitive(it) })
        }
    }

    private fun modelTypePayload(type: ModelType): JsonObject {
        return buildJsonObject {
            put("type_id", type.id.value.toString())
            put("key", type.key.value)
            putNullable("name", json(type.name))
            putNullable("description", json(type.description))
        }
    }

    private fun modelAggregateTypePayload(type: StoreModelAggregateType): JsonObject {
        return buildJsonObject {
            put("type_id", type.id.value.toString())
            put("key", type.key.value)
            putNullable("name", json(type.name))
            putNullable("description", json(type.description))
        }
    }

    private fun modelTypeInitializerPayload(initializer: ModelTypeInitializer): JsonObject {
        return buildJsonObject {
            put("key", initializer.key.value)
            putNullable("name", json(initializer.name))
            putNullable("description", json(initializer.description))
        }
    }

    private fun entityPayload(entity: Entity, attributes: List<Attribute>): JsonObject {
        return buildJsonObject {
            put("entity_id", entity.id.value.toString())
            put("key", entity.key.value)
            putNullable("name", json(entity.name))
            putNullable("description", json(entity.description))
            put("identifier_attribute_id", entity.identifierAttributeId.value.toString())
            put("origin", entityOriginPayload(entity.origin))
            putNullable("documentation_home", entity.documentationHome?.toExternalForm()?.let { JsonPrimitive(it) })
            putJsonArray("tags") {
                entity.tags.sortedBy { it.value.toString() }.forEach { add(JsonPrimitive(it.value.toString())) }
            }
            putJsonArray("attributes") {
                attributes.sortedBy { it.key.value }.forEach { add(attributePayload(it)) }
            }
        }
    }

    private fun modelAggregateEntityPayload(
        entity: StoreModelAggregateEntity,
        attributes: List<StoreModelAggregateEntityAttribute>
    ): JsonObject {
        return buildJsonObject {
            put("entity_id", entity.id.value.toString())
            put("key", entity.key.value)
            putNullable("name", json(entity.name))
            putNullable("description", json(entity.description))
            put("identifier_attribute_id", entity.identifierAttributeId.value.toString())
            put("origin", entityOriginPayload(entity.origin))
            putNullable("documentation_home", entity.documentationHome?.toExternalForm()?.let { JsonPrimitive(it) })
            putJsonArray("attributes") {
                attributes.sortedBy { it.key.value }.forEach { add(modelAggregateEntityAttributePayload(it)) }
            }
        }
    }

    private fun relationshipPayload(relationship: Relationship, attributes: List<Attribute>): JsonObject {
        return buildJsonObject {
            put("relationship_id", relationship.id.value.toString())
            put("key", relationship.key.value)
            putNullable("name", json(relationship.name))
            putNullable("description", json(relationship.description))
            putJsonArray("tags") {
                relationship.tags.sortedBy { it.value.toString() }.forEach { add(JsonPrimitive(it.value.toString())) }
            }
            putJsonArray("roles") {
                relationship.roles.sortedBy { it.key.value }.forEach { add(relationshipRolePayload(it)) }
            }
            putJsonArray("attributes") {
                attributes.sortedBy { it.key.value }.forEach { add(attributePayload(it)) }
            }
        }
    }

    private fun modelAggregateRelationshipPayload(
        relationship: StoreModelAggregateRelationship,
        attributes: List<StoreModelAggregateRelationshipAttribute>
    ): JsonObject {
        return buildJsonObject {
            put("relationship_id", relationship.id.value.toString())
            put("key", relationship.key.value)
            putNullable("name", json(relationship.name))
            putNullable("description", json(relationship.description))
            putJsonArray("roles") {
                relationship.roles.sortedBy { it.key.value }.forEach { add(modelAggregateRelationshipRolePayload(it)) }
            }
            putJsonArray("attributes") {
                attributes.sortedBy { it.key.value }.forEach { add(modelAggregateRelationshipAttributePayload(it)) }
            }
        }
    }

    private fun relationshipRolePayload(role: RelationshipRole): JsonObject {
        return buildJsonObject {
            put("role_id", role.id.value.toString())
            put("key", role.key.value)
            put("entity_id", role.entityId.value.toString())
            putNullable("name", json(role.name))
            put("cardinality", role.cardinality.code)
        }
    }

    private fun modelAggregateRelationshipRolePayload(role: StoreModelAggregateRelationshipRole): JsonObject {
        return buildJsonObject {
            put("role_id", role.id.value.toString())
            put("key", role.key.value)
            put("entity_id", role.entityId.value.toString())
            putNullable("name", json(role.name))
            put("cardinality", role.cardinality.code)
        }
    }

    private fun attributePayload(attribute: Attribute): JsonObject {
        return buildJsonObject {
            put("attribute_id", attribute.id.value.toString())
            put("owner", ownerPayload(attribute.ownerId))
            put("key", attribute.key.value)
            putNullable("name", json(attribute.name))
            putNullable("description", json(attribute.description))
            put("type_id", attribute.typeId.value.toString())
            put("optional", attribute.optional)
            putJsonArray("tags") {
                attribute.tags.sortedBy { it.value.toString() }.forEach { add(JsonPrimitive(it.value.toString())) }
            }
        }
    }

    private fun modelAggregateEntityAttributePayload(attribute: StoreModelAggregateEntityAttribute): JsonObject {
        return buildJsonObject {
            put("attribute_id", attribute.id.value.toString())
            put("key", attribute.key.value)
            putNullable("name", json(attribute.name))
            putNullable("description", json(attribute.description))
            put("type_id", attribute.typeId.value.toString())
            put("optional", attribute.optional)
        }
    }

    private fun modelAggregateRelationshipAttributePayload(attribute: StoreModelAggregateRelationshipAttribute): JsonObject {
        return buildJsonObject {
            put("attribute_id", attribute.id.value.toString())
            put("key", attribute.key.value)
            putNullable("name", json(attribute.name))
            putNullable("description", json(attribute.description))
            put("type_id", attribute.typeId.value.toString())
            put("optional", attribute.optional)
        }
    }

    private fun ownerPayload(owner: AttributeOwnerId): JsonObject {
        return when (owner) {
            is AttributeOwnerId.OwnerEntityId -> buildJsonObject {
                put("owner_type", "entity")
                put("owner_id", owner.id.value.toString())
            }
            is AttributeOwnerId.OwnerRelationshipId -> buildJsonObject {
                put("owner_type", "relationship")
                put("owner_id", owner.id.value.toString())
            }
        }
    }

    private fun modelOriginPayload(origin: ModelOrigin): JsonObject {
        return when (origin) {
            is ModelOrigin.Manual -> buildJsonObject { put("origin_type", "manual") }
            is ModelOrigin.Uri -> buildJsonObject {
                put("origin_type", "uri")
                put("uri", origin.uri.toString())
            }
        }
    }

    private fun entityOriginPayload(origin: EntityOrigin): JsonObject {
        return when (origin) {
            is EntityOrigin.Manual -> buildJsonObject { put("origin_type", "manual") }
            is EntityOrigin.Uri -> buildJsonObject {
                put("origin_type", "uri")
                put("uri", origin.uri.toString())
            }
        }
    }

    private fun json(value: LocalizedText?): JsonElement? {
        return value?.let { localizedTextPayload(it.all()) }
    }

    private fun json(value: LocalizedMarkdown?): JsonElement? {
        return value?.let { localizedTextPayload(it.all()) }
    }

    private fun localizedTextPayload(values: Map<String, String>): JsonObject {
        return buildJsonObject {
            putJsonObject("values") {
                values.toSortedMap().forEach { (lang, text) -> put(lang, text) }
            }
        }
    }

    private fun event(type: String, payload: JsonObject, modelVersion: String? = null): EventData {
        return EventData(type = type, payload = payload, modelVersion = modelVersion)
    }

    private data class EventData(
        val type: String,
        val payload: JsonObject,
        val modelVersion: String?,
    )

    companion object {
        private const val EVENT_VERSION = 1
    }
}

private fun JsonObjectBuilder.putNullable(key: String, value: JsonElement?) {
    if (value == null) {
        put(key, JsonNull)
        return
    }
    put(key, value)
}
