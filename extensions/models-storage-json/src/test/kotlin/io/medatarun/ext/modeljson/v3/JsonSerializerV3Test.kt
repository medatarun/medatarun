package io.medatarun.ext.modeljson.v3

import io.kotest.assertions.json.shouldEqualJson
import io.medatarun.ext.modeljson.ModelJsonSchemas
import io.medatarun.ext.modeljson.internal.ModelJsonConverter
import io.medatarun.model.domain.*
import io.medatarun.model.infra.ModelAggregateInMemory
import io.medatarun.model.infra.RelationshipInMemory
import io.medatarun.model.infra.RelationshipRoleInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.infra.inmemory.PBKeyParticipantInMemory
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.id.Id
import java.net.URI
import kotlin.test.Test

internal class JsonSerializerV3Test {

    val converter = ModelJsonConverter(prettyPrint = false)

    @Test
    fun should_copy_model_root_fields_to_json_output() {

        val modelId = Id.generate(::ModelId)

        val model = ModelAggregateInMemory.builder(
            ModelInMemory(
                key = ModelKey("root-fields-test"),
                version = ModelVersion("1.2.3"),
                id = modelId,
                name = null,
                description = null,
                origin = ModelOrigin.Manual,
                authority = ModelAuthority.CANONICAL,
                documentationHome = URI("https://docs.example.org/model").toURL()
            )
        )

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"root-fields-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.2.3",
              "authority":"canonical",
              "documentationHome":"https://docs.example.org/model"
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_multiple_types_with_and_without_name_description() {
        val modelId = Id.generate(::ModelId)
        val typeWithLabelsId = Id.generate(::TypeId)
        val typeWithoutLabelsId = Id.generate(::TypeId)

        val model = ModelAggregateInMemory.builder(
            id = modelId,
            key = ModelKey("types-test"),
            version = ModelVersion("1.0.0")
        ) {
            addType(
                id = typeWithLabelsId,
                key = TypeKey("string"),
                name = LocalizedTextNotLocalized("String"),
                description = LocalizedMarkdownNotLocalized("String value type")
            )
            addType(
                id = typeWithoutLabelsId,
                key = TypeKey("uuid"),
                description = null
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"types-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[
                {"id":"${typeWithLabelsId.asString()}","key":"string","name":"String","description":"String value type"},
                {"id":"${typeWithoutLabelsId.asString()}","key":"uuid"}
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_tags_for_model_entities_attributes_and_relationships() {
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityWithTagId = Id.generate(::EntityId)
        val entityWithoutTagId = Id.generate(::EntityId)
        val entityWithTagAttributeId = Id.generate(::AttributeId)
        val entityWithoutTagAttributeId = Id.generate(::AttributeId)
        val relationshipId = Id.generate(::RelationshipId)
        val relationshipRoleId = Id.generate(::RelationshipRoleId)

        val modelTagA = Id.generate(::TagId)
        val modelTagB = Id.generate(::TagId)
        val entityTag = Id.generate(::TagId)
        val attributeTag = Id.generate(::TagId)
        val relationshipTag = Id.generate(::TagId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("tags-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(key = TypeKey("text"), id = typeId, name = null, description = null)
            addEntity(
                id = entityWithTagId,
                key = EntityKey("customer"),
                identifierAttributeId = entityWithTagAttributeId,
                tags = mutableListOf(entityTag)
            )
            addEntity(
                id = entityWithoutTagId,
                key = EntityKey("order"),
                identifierAttributeId = entityWithoutTagAttributeId
            )
            addAttribute(
                id = entityWithTagAttributeId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityWithTagId),
                key = AttributeKey("customer-code"),
                typeId = typeId,
                tags = listOf(attributeTag)
            )
            addAttribute(
                id = entityWithoutTagAttributeId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityWithoutTagId),
                key = AttributeKey("order-code"),
                typeId = typeId
            )
            addRelationship(
                id = relationshipId,
                key = RelationshipKey("customer-order"),
                roles = listOf(
                    RelationshipRoleInMemory(
                        id = relationshipRoleId,
                        key = RelationshipRoleKey("customer-role"),
                        entityId = entityWithTagId,
                        name = null,
                        cardinality = RelationshipCardinality.One
                    )
                ),
                tags = listOf(relationshipTag)
            )
            addTag(modelTagA)
            addTag(modelTagB)
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"tags-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "tags":["${modelTagA.asString()}","${modelTagB.asString()}"],
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[
                {
                  "id":"${entityWithTagId.asString()}",
                  "key":"customer",
                  "attributes":[{"id":"${entityWithTagAttributeId.asString()}","key":"customer-code","type":"key:text","tags":["${attributeTag.asString()}"]}],
                  "tags":["${entityTag.asString()}"]
                },
                {
                  "id":"${entityWithoutTagId.asString()}",
                  "key":"order",
                  "attributes":[{"id":"${entityWithoutTagAttributeId.asString()}","key":"order-code","type":"key:text"}]
                }
              ],
              "relationships":[
                {
                  "id":"${relationshipId.asString()}",
                  "key":"customer-order",
                  "roles":[{"id":"${relationshipRoleId.asString()}","key":"customer-role","entity":"key:customer","cardinality":"one"}],
                  "tags":["${relationshipTag.asString()}"]
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_entity_with_name_description_origin_and_documentation_home() {
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val attrId = Id.generate(::AttributeId)
        val entityOrigin = "https://example.org/entities/customer"
        val entityDoc = "https://docs.example.org/entities/customer"

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("entity-fields-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(
                id = entityId,
                key = EntityKey("customer"),
                name = LocalizedTextNotLocalized("Customer"),
                description = LocalizedMarkdownNotLocalized("Customer description"),
                identifierAttributeId = attrId,
                origin = EntityOrigin.Uri(URI(entityOrigin)),
                documentationHome = URI(entityDoc).toURL()
            )
            addAttribute(
                id = attrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("customer-code"),
                typeId = typeId
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"entity-fields-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[
                {
                  "id":"${entityId.asString()}",
                  "key":"customer",
                  "name":"Customer",
                  "description":"Customer description",
                  "origin":"$entityOrigin",
                  "attributes":[{"id":"${attrId.asString()}","key":"customer-code","type":"key:text"}],
                  "documentationHome":"$entityDoc"
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_entity_without_optional_fields() {
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val attrId = Id.generate(::AttributeId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("entity-without-optionals-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(
                id = entityId,
                key = EntityKey("customer"),
                name = null,
                description = null,
                identifierAttributeId = attrId,
                origin = EntityOrigin.Manual,
                documentationHome = null,
                tags = mutableListOf()
            )
            addAttribute(
                id = attrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("customer-code"),
                typeId = typeId
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"entity-without-optionals-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[
                {"id":"${entityId.asString()}","key":"customer","attributes":[{"id":"${attrId.asString()}","key":"customer-code","type":"key:text"}]}
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_only_attributes_owned_by_each_entity() {
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityAId = Id.generate(::EntityId)
        val entityBId = Id.generate(::EntityId)
        val attrAId = Id.generate(::AttributeId)
        val attrBId = Id.generate(::AttributeId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("entity-ownership-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityAId, key = EntityKey("a"), identifierAttributeId = attrAId)
            addEntity(id = entityBId, key = EntityKey("b"), identifierAttributeId = attrBId)
            addAttribute(
                id = attrAId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityAId),
                key = AttributeKey("a-code"),
                typeId = typeId
            )
            addAttribute(
                id = attrBId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityBId),
                key = AttributeKey("b-code"),
                typeId = typeId
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"entity-ownership-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[
                {"id":"${entityAId.asString()}","key":"a","attributes":[{"id":"${attrAId.asString()}","key":"a-code","type":"key:text"}]},
                {"id":"${entityBId.asString()}","key":"b","attributes":[{"id":"${attrBId.asString()}","key":"b-code","type":"key:text"}]}
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_entity_attribute_fields() {
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val attrId = Id.generate(::AttributeId)
        val attrTag = Id.generate(::TagId)

        val model = ModelAggregateInMemory.builder(
            id = modelId,
            key = ModelKey("entity-attribute-fields-test"),
            version = ModelVersion("1.0.0"),
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityId, key = EntityKey("customer"), identifierAttributeId = attrId)
            addAttribute(
                id = attrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("customer-code"),
                name = LocalizedTextNotLocalized("Customer Code"),
                description = LocalizedMarkdownNotLocalized("Unique customer code"),
                typeId = typeId,
                optional = true,
                tags = listOf(attrTag)
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"entity-attribute-fields-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[
                {
                  "id":"${entityId.asString()}",
                  "key":"customer",
                  "attributes":[
                    {
                      "id":"${attrId.asString()}",
                      "key":"customer-code",
                      "name":"Customer Code",
                      "description":"Unique customer code",
                      "type":"key:text",
                      "optional":true,
                      "tags":["${attrTag.asString()}"]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_entity_primary_key_sorted_by_position() {
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val attrFirstId = Id.generate(::AttributeId)
        val attrSecondId = Id.generate(::AttributeId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("entity-primary-key-sorted-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityId, key = EntityKey("customer"), identifierAttributeId = attrFirstId)
            addAttribute(
                id = attrFirstId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("code"),
                typeId = typeId
            )
            addAttribute(
                id = attrSecondId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("country"),
                typeId = typeId
            )
            addEntityPrimaryKey(
                id = Id.generate(::EntityPrimaryKeyId),
                entityId = entityId,
                participants = listOf(
                    PBKeyParticipantInMemory(attributeId = attrSecondId, position = 2),
                    PBKeyParticipantInMemory(attributeId = attrFirstId, position = 1)
                )
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"entity-primary-key-sorted-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[
                {
                  "id":"${entityId.asString()}",
                  "key":"customer",
                  "attributes":[
                    {"id":"${attrFirstId.asString()}","key":"code","type":"key:text"},
                    {"id":"${attrSecondId.asString()}","key":"country","type":"key:text"}
                  ],
                  "primaryKey":["${attrFirstId.asString()}","${attrSecondId.asString()}"]
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_not_fail_when_entity_has_no_primary_key() {
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val attrId = Id.generate(::AttributeId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("entity-primary-key-empty-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityId, key = EntityKey("customer"), identifierAttributeId = attrId)
            addAttribute(
                id = attrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("code"),
                typeId = typeId
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"entity-primary-key-empty-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[
                {"id":"${entityId.asString()}","key":"customer","attributes":[{"id":"${attrId.asString()}","key":"code","type":"key:text"}]}
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_relationship_with_name_description_and_tags() {
        // Why: lock relationship root-field mapping (id/key/name/description/tags) in exported JSON.
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val attrId = Id.generate(::AttributeId)
        val relationshipId = Id.generate(::RelationshipId)
        val roleId = Id.generate(::RelationshipRoleId)
        val relationshipTag = Id.generate(::TagId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("relationship-root-fields-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityId, key = EntityKey("customer"), identifierAttributeId = attrId)
            addAttribute(
                id = attrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("code"),
                typeId = typeId
            )
            addRelationship(
                id = relationshipId,
                key = RelationshipKey("customer-order"),
                name = LocalizedTextNotLocalized("Customer Order"),
                description = LocalizedMarkdownNotLocalized("Links customers to orders"),
                roles = listOf(
                    RelationshipRoleInMemory(
                        id = roleId,
                        key = RelationshipRoleKey("customer-role"),
                        entityId = entityId,
                        name = null,
                        cardinality = RelationshipCardinality.One
                    )
                ),
                tags = listOf(relationshipTag)
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"relationship-root-fields-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[{"id":"${entityId.asString()}","key":"customer","attributes":[{"id":"${attrId.asString()}","key":"code","type":"key:text"}]}],
              "relationships":[
                {
                  "id":"${relationshipId.asString()}",
                  "key":"customer-order",
                  "name":"Customer Order",
                  "description":"Links customers to orders",
                  "roles":[{"id":"${roleId.asString()}","key":"customer-role","entity":"key:customer","cardinality":"one"}],
                  "tags":["${relationshipTag.asString()}"]
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_relationship_roles_with_entity_key_and_cardinality() {
        // Why: ensure role.entityId is exported as entity key and role cardinality/name are mapped correctly.
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val attrId = Id.generate(::AttributeId)
        val relationshipId = Id.generate(::RelationshipId)
        val roleId = Id.generate(::RelationshipRoleId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("relationship-roles-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityId, key = EntityKey("customer"), identifierAttributeId = attrId)
            addAttribute(
                id = attrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("code"),
                typeId = typeId
            )
            addRelationship(
                id = relationshipId,
                key = RelationshipKey("rel"),
                name = null,
                description = null,
                roles = listOf(
                    RelationshipRoleInMemory(
                        id = roleId,
                        key = RelationshipRoleKey("customer-role"),
                        entityId = entityId,
                        name = LocalizedTextNotLocalized("Customer"),
                        cardinality = RelationshipCardinality.Many
                    )
                ),
                tags = emptyList()
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"relationship-roles-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[{"id":"${entityId.asString()}","key":"customer","attributes":[{"id":"${attrId.asString()}","key":"code","type":"key:text"}]}],
              "relationships":[
                {
                  "id":"${relationshipId.asString()}",
                  "key":"rel",
                  "roles":[
                    {
                      "id":"${roleId.asString()}",
                      "key":"customer-role",
                      "entity":"key:customer",
                      "name":"Customer",
                      "cardinality":"many"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_only_attributes_owned_by_relationship() {
        // Why: verify relationship attribute export filters by relationship ownership only.
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val entityAttrId = Id.generate(::AttributeId)
        val relationshipId = Id.generate(::RelationshipId)
        val relationshipAttrId = Id.generate(::AttributeId)
        val roleId = Id.generate(::RelationshipRoleId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("relationship-owned-attributes-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityId, key = EntityKey("customer"), identifierAttributeId = entityAttrId)
            addAttribute(
                id = entityAttrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("entity-code"),
                typeId = typeId
            )
            addAttribute(
                id = relationshipAttrId,
                ownerId = AttributeOwnerId.OwnerRelationshipId(relationshipId),
                key = AttributeKey("relationship-code"),
                typeId = typeId
            )
            addRelationship(
                id = relationshipId,
                key = RelationshipKey("rel"),
                roles = listOf(
                    RelationshipRoleInMemory(
                        id = roleId,
                        key = RelationshipRoleKey("customer-role"),
                        entityId = entityId,
                        name = null,
                        cardinality = RelationshipCardinality.One
                    )
                ),
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"relationship-owned-attributes-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[{"id":"${entityId.asString()}","key":"customer","attributes":[{"id":"${entityAttrId.asString()}","key":"entity-code","type":"key:text"}]}],
              "relationships":[
                {
                  "id":"${relationshipId.asString()}",
                  "key":"rel",
                  "roles":[{"id":"${roleId.asString()}","key":"customer-role","entity":"key:customer","cardinality":"one"}],
                  "attributes":[{"id":"${relationshipAttrId.asString()}","key":"relationship-code","type":"key:text"}]
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_relationship_attribute_fields() {
        // Why: lock complete mapping of relationship attribute fields including optional and tags.
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val entityAttrId = Id.generate(::AttributeId)
        val relationshipId = Id.generate(::RelationshipId)
        val relationshipAttrId = Id.generate(::AttributeId)
        val roleId = Id.generate(::RelationshipRoleId)
        val attrTag = Id.generate(::TagId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("relationship-attribute-fields-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityId, key = EntityKey("customer"), identifierAttributeId = entityAttrId)
            addAttribute(
                id = entityAttrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("entity-code"),
                typeId = typeId
            )
            addAttribute(
                id = relationshipAttrId,
                ownerId = AttributeOwnerId.OwnerRelationshipId(relationshipId),
                key = AttributeKey("relationship-code"),
                name = LocalizedTextNotLocalized("Relationship Code"),
                description = LocalizedMarkdownNotLocalized("Relationship attribute description"),
                typeId = typeId,
                optional = true,
                tags = listOf(attrTag)
            )
            addRelationship(
                id = relationshipId,
                key = RelationshipKey("rel"),
                roles = listOf(
                    RelationshipRoleInMemory(
                        id = roleId,
                        key = RelationshipRoleKey("customer-role"),
                        entityId = entityId,
                        name = null,
                        cardinality = RelationshipCardinality.One
                    )
                ),
                tags = emptyList()
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"relationship-attribute-fields-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[{"id":"${entityId.asString()}","key":"customer","attributes":[{"id":"${entityAttrId.asString()}","key":"entity-code","type":"key:text"}]}],
              "relationships":[
                {
                  "id":"${relationshipId.asString()}",
                  "key":"rel",
                  "roles":[{"id":"${roleId.asString()}","key":"customer-role","entity":"key:customer","cardinality":"one"}],
                  "attributes":[
                    {
                      "id":"${relationshipAttrId.asString()}",
                      "key":"relationship-code",
                      "name":"Relationship Code",
                      "description":"Relationship attribute description",
                      "type":"key:text",
                      "optional":true,
                      "tags":["${attrTag.asString()}"]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_relationship_without_optional_fields() {
        // Why: ensure relation export stays valid when name/description/tags are not set and attributes are empty.
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val attrId = Id.generate(::AttributeId)
        val relationshipId = Id.generate(::RelationshipId)
        val roleId = Id.generate(::RelationshipRoleId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("relationship-without-optionals-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityId, key = EntityKey("customer"), identifierAttributeId = attrId)
            addAttribute(
                id = attrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("code"),
                typeId = typeId
            )
            addRelationship(
                RelationshipInMemory(
                    id = relationshipId,
                    key = RelationshipKey("rel"),
                    name = null,
                    description = null,
                    roles = listOf(
                        RelationshipRoleInMemory(
                            id = roleId,
                            key = RelationshipRoleKey("customer-role"),
                            entityId = entityId,
                            name = null,
                            cardinality = RelationshipCardinality.One
                        )
                    ),
                    tags = emptyList()
                )
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"relationship-without-optionals-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[{"id":"${entityId.asString()}","key":"customer","attributes":[{"id":"${attrId.asString()}","key":"code","type":"key:text"}]}],
              "relationships":[
                {
                  "id":"${relationshipId.asString()}",
                  "key":"rel",
                  "roles":[{"id":"${roleId.asString()}","key":"customer-role","entity":"key:customer","cardinality":"one"}]
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_multiple_relationships_independently() {
        // Why: ensure no data leakage between multiple relationships (roles/attributes/tags stay scoped).
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityAId = Id.generate(::EntityId)
        val entityBId = Id.generate(::EntityId)
        val entityAAttrId = Id.generate(::AttributeId)
        val entityBAttrId = Id.generate(::AttributeId)
        val relationshipAId = Id.generate(::RelationshipId)
        val relationshipBId = Id.generate(::RelationshipId)
        val relationshipAAttrId = Id.generate(::AttributeId)
        val relationshipBAttrId = Id.generate(::AttributeId)
        val roleAId = Id.generate(::RelationshipRoleId)
        val roleBId = Id.generate(::RelationshipRoleId)
        val relTagA = Id.generate(::TagId)
        val relTagB = Id.generate(::TagId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("multiple-relationships-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityAId, key = EntityKey("customer"), identifierAttributeId = entityAAttrId)
            addEntity(id = entityBId, key = EntityKey("order"), identifierAttributeId = entityBAttrId)
            addAttribute(
                id = entityAAttrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityAId),
                key = AttributeKey("customer-code"),
                typeId = typeId
            )
            addAttribute(
                id = entityBAttrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityBId),
                key = AttributeKey("order-code"),
                typeId = typeId
            )
            addAttribute(
                id = relationshipAAttrId,
                ownerId = AttributeOwnerId.OwnerRelationshipId(relationshipAId),
                key = AttributeKey("ra"),
                typeId = typeId
            )
            addAttribute(
                id = relationshipBAttrId,
                ownerId = AttributeOwnerId.OwnerRelationshipId(relationshipBId),
                key = AttributeKey("rb"),
                typeId = typeId
            )
            addRelationship(
                RelationshipInMemory(
                    id = relationshipAId,
                    key = RelationshipKey("rel-a"),
                    name = null,
                    description = null,
                    roles = listOf(
                        RelationshipRoleInMemory(
                            id = roleAId,
                            key = RelationshipRoleKey("role-a"),
                            entityId = entityAId,
                            name = null,
                            cardinality = RelationshipCardinality.One
                        )
                    ),
                    tags = listOf(relTagA)
                )
            )
            addRelationship(
                RelationshipInMemory(
                    id = relationshipBId,
                    key = RelationshipKey("rel-b"),
                    name = null,
                    description = null,
                    roles = listOf(
                        RelationshipRoleInMemory(
                            id = roleBId,
                            key = RelationshipRoleKey("role-b"),
                            entityId = entityBId,
                            name = null,
                            cardinality = RelationshipCardinality.Many
                        )
                    ),
                    tags = listOf(relTagB)
                )
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"multiple-relationships-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[
                {"id":"${entityAId.asString()}","key":"customer","attributes":[{"id":"${entityAAttrId.asString()}","key":"customer-code","type":"key:text"}]},
                {"id":"${entityBId.asString()}","key":"order","attributes":[{"id":"${entityBAttrId.asString()}","key":"order-code","type":"key:text"}]}
              ],
              "relationships":[
                {
                  "id":"${relationshipAId.asString()}",
                  "key":"rel-a",
                  "roles":[{"id":"${roleAId.asString()}","key":"role-a","entity":"key:customer","cardinality":"one"}],
                  "attributes":[{"id":"${relationshipAAttrId.asString()}","key":"ra","type":"key:text"}],
                  "tags":["${relTagA.asString()}"]
                },
                {
                  "id":"${relationshipBId.asString()}",
                  "key":"rel-b",
                  "roles":[{"id":"${roleBId.asString()}","key":"role-b","entity":"key:order","cardinality":"many"}],
                  "attributes":[{"id":"${relationshipBAttrId.asString()}","key":"rb","type":"key:text"}],
                  "tags":["${relTagB.asString()}"]
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_business_key_with_all_fields() {
        // Why: lock full export mapping of business key fields.
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val attrFirstId = Id.generate(::AttributeId)
        val attrSecondId = Id.generate(::AttributeId)
        val businessKeyId = Id.generate(::BusinessKeyId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("business-key-full-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityId, key = EntityKey("customer"), identifierAttributeId = attrFirstId)
            addAttribute(
                id = attrFirstId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("code"),
                typeId = typeId
            )
            addAttribute(
                id = attrSecondId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("country"),
                typeId = typeId
            )
            addBusinessKey(
                id = businessKeyId,
                key = BusinessKeyKey("customer-bk"),
                entityId = entityId,
                participants = listOf(
                    PBKeyParticipantInMemory(attributeId = attrFirstId, position = 1),
                    PBKeyParticipantInMemory(attributeId = attrSecondId, position = 2)
                ),
                name = "Customer BK",
                description = "Uniquely identifies customer"
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"business-key-full-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[{"id":"${entityId.asString()}","key":"customer","attributes":[{"id":"${attrFirstId.asString()}","key":"code","type":"key:text"},{"id":"${attrSecondId.asString()}","key":"country","type":"key:text"}]}],
              "businessKeys":[
                {
                  "id":"${businessKeyId.asString()}",
                  "key":"customer-bk",
                  "entity":"key:customer",
                  "participants":["key:code","key:country"],
                  "name":"Customer BK",
                  "description":"Uniquely identifies customer"
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_business_key_without_optional_name_and_description() {
        // Why: ensure export stays valid when optional business key labels are not set.
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val attrId = Id.generate(::AttributeId)
        val businessKeyId = Id.generate(::BusinessKeyId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("business-key-without-optionals-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityId, key = EntityKey("customer"), identifierAttributeId = attrId)
            addAttribute(
                id = attrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("code"),
                typeId = typeId
            )
            addBusinessKey(
                id = businessKeyId,
                key = BusinessKeyKey("customer-bk"),
                entityId = entityId,
                participants = listOf(PBKeyParticipantInMemory(attributeId = attrId, position = 1)),
                name = null,
                description = null
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"business-key-without-optionals-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[{"id":"${entityId.asString()}","key":"customer","attributes":[{"id":"${attrId.asString()}","key":"code","type":"key:text"}]}],
              "businessKeys":[
                {
                  "id":"${businessKeyId.asString()}",
                  "key":"customer-bk",
                  "entity":"key:customer",
                  "participants":["key:code"]
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_sort_business_key_participants_by_position() {
        // Why: lock participant ordering contract for business keys.
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val attrFirstId = Id.generate(::AttributeId)
        val attrSecondId = Id.generate(::AttributeId)
        val businessKeyId = Id.generate(::BusinessKeyId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("business-key-participants-order-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityId, key = EntityKey("customer"), identifierAttributeId = attrFirstId)
            addAttribute(
                id = attrFirstId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("code"),
                typeId = typeId
            )
            addAttribute(
                id = attrSecondId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("country"),
                typeId = typeId
            )
            addBusinessKey(
                id = businessKeyId,
                key = BusinessKeyKey("customer-bk"),
                entityId = entityId,
                participants = listOf(
                    PBKeyParticipantInMemory(attributeId = attrSecondId, position = 2),
                    PBKeyParticipantInMemory(attributeId = attrFirstId, position = 1)
                ),
                name = null,
                description = null
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"business-key-participants-order-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[{"id":"${entityId.asString()}","key":"customer","attributes":[{"id":"${attrFirstId.asString()}","key":"code","type":"key:text"},{"id":"${attrSecondId.asString()}","key":"country","type":"key:text"}]}],
              "businessKeys":[
                {
                  "id":"${businessKeyId.asString()}",
                  "key":"customer-bk",
                  "entity":"key:customer",
                  "participants":["key:code","key:country"]
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_multiple_business_keys_independently() {
        // Why: ensure multiple business keys do not leak participants or labels between each other.
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityAId = Id.generate(::EntityId)
        val entityBId = Id.generate(::EntityId)
        val attrAId = Id.generate(::AttributeId)
        val attrBId = Id.generate(::AttributeId)
        val businessKeyAId = Id.generate(::BusinessKeyId)
        val businessKeyBId = Id.generate(::BusinessKeyId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("multiple-business-keys-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityAId, key = EntityKey("customer"), identifierAttributeId = attrAId)
            addEntity(id = entityBId, key = EntityKey("order"), identifierAttributeId = attrBId)
            addAttribute(
                id = attrAId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityAId),
                key = AttributeKey("customer-code"),
                typeId = typeId
            )
            addAttribute(
                id = attrBId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityBId),
                key = AttributeKey("order-code"),
                typeId = typeId
            )
            addBusinessKey(
                id = businessKeyAId,
                key = BusinessKeyKey("customer-bk"),
                entityId = entityAId,
                participants = listOf(PBKeyParticipantInMemory(attributeId = attrAId, position = 1)),
                name = "Customer BK",
                description = "Customer key"
            )
            addBusinessKey(
                id = businessKeyBId,
                key = BusinessKeyKey("order-bk"),
                entityId = entityBId,
                participants = listOf(PBKeyParticipantInMemory(attributeId = attrBId, position = 1)),
                name = "Order BK",
                description = "Order key"
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"multiple-business-keys-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[
                {"id":"${entityAId.asString()}","key":"customer","attributes":[{"id":"${attrAId.asString()}","key":"customer-code","type":"key:text"}]},
                {"id":"${entityBId.asString()}","key":"order","attributes":[{"id":"${attrBId.asString()}","key":"order-code","type":"key:text"}]}
              ],
              "businessKeys":[
                {
                  "id":"${businessKeyAId.asString()}",
                  "key":"customer-bk",
                  "entity":"key:customer",
                  "participants":["key:customer-code"],
                  "name":"Customer BK",
                  "description":"Customer key"
                },
                {
                  "id":"${businessKeyBId.asString()}",
                  "key":"order-bk",
                  "entity":"key:order",
                  "participants":["key:order-code"],
                  "name":"Order BK",
                  "description":"Order key"
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_write_business_keys_as_empty_array_when_none() {
        // Why: lock current export contract when no business key exists (field omitted).
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val attrId = Id.generate(::AttributeId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("business-keys-empty-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityId, key = EntityKey("customer"), identifierAttributeId = attrId)
            addAttribute(
                id = attrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("code"),
                typeId = typeId
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"business-keys-empty-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[{"id":"${entityId.asString()}","key":"customer","attributes":[{"id":"${attrId.asString()}","key":"code","type":"key:text"}]}]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    @Test
    fun should_keep_business_key_entity_id_as_entity_uuid() {
        // Why: prevent regression where entityId could be exported as key instead of UUID.
        val modelId = Id.generate(::ModelId)
        val typeId = Id.generate(::TypeId)
        val entityId = Id.generate(::EntityId)
        val attrId = Id.generate(::AttributeId)
        val businessKeyId = Id.generate(::BusinessKeyId)

        val model = ModelAggregateInMemory.builder(
            key = ModelKey("business-key-entity-id-format-test"),
            version = ModelVersion("1.0.0"),
            id = modelId
        ) {
            addType(id = typeId, key = TypeKey("text"))
            addEntity(id = entityId, key = EntityKey("customer"), identifierAttributeId = attrId)
            addAttribute(
                id = attrId,
                ownerId = AttributeOwnerId.OwnerEntityId(entityId),
                key = AttributeKey("code"),
                typeId = typeId
            )
            addBusinessKey(
                id = businessKeyId,
                key = BusinessKeyKey("customer-bk"),
                entityId = entityId,
                participants = listOf(PBKeyParticipantInMemory(attributeId = attrId, position = 1)),
                name = null,
                description = null
            )
        }

        val actualJson = converter.toJsonStringV3(model)
        val expectedJson = """
            {
              "id":"${modelId.asString()}",
              "key":"business-key-entity-id-format-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.0.0",
              "authority":"system",
              "types":[{"id":"${typeId.asString()}","key":"text"}],
              "entities":[{"id":"${entityId.asString()}","key":"customer","attributes":[{"id":"${attrId.asString()}","key":"code","type":"key:text"}]}],
              "businessKeys":[
                {
                  "id":"${businessKeyId.asString()}",
                  "key":"customer-bk",
                  "entity":"key:customer",
                  "participants":["key:code"]
                }
              ]
            }
        """.trimIndent()

        assertJson(expectedJson, actualJson)
    }

    private fun assertJson(expectedJson: String, actualJson: String) {
        actualJson shouldEqualJson expectedJson
    }
}
