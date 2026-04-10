package io.medatarun.ext.modeljson.v3

import io.kotest.assertions.json.shouldEqualJson
import io.medatarun.ext.modeljson.ModelJsonSchemas
import io.medatarun.ext.modeljson.internal.ModelJsonConverter
import io.medatarun.model.domain.*
import io.medatarun.model.infra.AttributeInMemory
import io.medatarun.model.infra.EntityInMemory
import io.medatarun.model.infra.ModelAggregateInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.infra.RelationshipInMemory
import io.medatarun.model.infra.RelationshipRoleInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.id.Id
import java.net.URI
import kotlin.test.Test

internal class JsonSerializerV3Test {

    val converter = ModelJsonConverter(prettyPrint = false)

    @Test
    fun should_copy_model_root_fields_to_json_output() {

        val modelId = Id.generate(::ModelId)

        val model = ModelAggregateInMemory(
            model = ModelInMemory(
                id = modelId,
                key = ModelKey("root-fields-test"),
                name = null,
                description = null,
                version = ModelVersion("1.2.3"),
                origin = ModelOrigin.Manual,
                authority = ModelAuthority.CANONICAL,
                documentationHome = URI("https://docs.example.org/model").toURL()
            ),
            types = emptyList(),
            entities = emptyList(),
            attributes = emptyList(),
            relationships = emptyList(),
            tags = emptyList(),
            entityPrimaryKeys = emptyList(),
            businessKeys = emptyList()
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

        val model = ModelAggregateInMemory(
            model = ModelInMemory(
                id = modelId,
                key = ModelKey("types-test"),
                name = null,
                description = null,
                version = ModelVersion("1.0.0"),
                origin = ModelOrigin.Manual,
                authority = ModelAuthority.SYSTEM,
                documentationHome = null
            ),
            types = listOf(
                ModelTypeInMemory(
                    id = typeWithLabelsId,
                    key = TypeKey("string"),
                    name = LocalizedTextNotLocalized("String"),
                    description = LocalizedMarkdownNotLocalized("String value type")
                ),
                ModelTypeInMemory(
                    id = typeWithoutLabelsId,
                    key = TypeKey("uuid"),
                    name = null,
                    description = null
                )
            ),
            entities = emptyList(),
            attributes = emptyList(),
            relationships = emptyList(),
            tags = emptyList(),
            entityPrimaryKeys = emptyList(),
            businessKeys = emptyList()
        )

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

        val model = ModelAggregateInMemory(
            model = ModelInMemory(
                id = modelId,
                key = ModelKey("tags-test"),
                name = null,
                description = null,
                version = ModelVersion("1.0.0"),
                origin = ModelOrigin.Manual,
                authority = ModelAuthority.SYSTEM,
                documentationHome = null
            ),
            types = listOf(
                ModelTypeInMemory(
                    id = typeId,
                    key = TypeKey("text"),
                    name = null,
                    description = null
                )
            ),
            entities = listOf(
                EntityInMemory(
                    id = entityWithTagId,
                    key = EntityKey("customer"),
                    name = null,
                    description = null,
                    identifierAttributeId = entityWithTagAttributeId,
                    origin = EntityOrigin.Manual,
                    documentationHome = null,
                    tags = listOf(entityTag)
                ),
                EntityInMemory(
                    id = entityWithoutTagId,
                    key = EntityKey("order"),
                    name = null,
                    description = null,
                    identifierAttributeId = entityWithoutTagAttributeId,
                    origin = EntityOrigin.Manual,
                    documentationHome = null,
                    tags = emptyList()
                )
            ),
            attributes = listOf(
                AttributeInMemory(
                    id = entityWithTagAttributeId,
                    ownerId = AttributeOwnerId.OwnerEntityId(entityWithTagId),
                    key = AttributeKey("customer-code"),
                    name = null,
                    description = null,
                    typeId = typeId,
                    optional = false,
                    tags = listOf(attributeTag)
                ),
                AttributeInMemory(
                    id = entityWithoutTagAttributeId,
                    ownerId = AttributeOwnerId.OwnerEntityId(entityWithoutTagId),
                    key = AttributeKey("order-code"),
                    name = null,
                    description = null,
                    typeId = typeId,
                    optional = false,
                    tags = emptyList()
                )
            ),
            relationships = listOf(
                RelationshipInMemory(
                    id = relationshipId,
                    key = RelationshipKey("customer-order"),
                    name = null,
                    description = null,
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
            ),
            tags = listOf(modelTagA, modelTagB),
            entityPrimaryKeys = emptyList(),
            businessKeys = emptyList()
        )

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
                  "attributes":[{"id":"${entityWithTagAttributeId.asString()}","key":"customer-code","type":"text","tags":["${attributeTag.asString()}"]}],
                  "tags":["${entityTag.asString()}"]
                },
                {
                  "id":"${entityWithoutTagId.asString()}",
                  "key":"order",
                  "attributes":[{"id":"${entityWithoutTagAttributeId.asString()}","key":"order-code","type":"text"}]
                }
              ],
              "relationships":[
                {
                  "id":"${relationshipId.asString()}",
                  "key":"customer-order",
                  "roles":[{"id":"${relationshipRoleId.asString()}","key":"customer-role","entityId":"customer","cardinality":"one"}],
                  "tags":["${relationshipTag.asString()}"]
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
