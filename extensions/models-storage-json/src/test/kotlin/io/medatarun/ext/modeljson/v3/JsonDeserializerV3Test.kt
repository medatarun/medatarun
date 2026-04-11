package io.medatarun.ext.modeljson.v3

import io.medatarun.ext.modeljson.ModelJsonSchemas
import io.medatarun.ext.modeljson.internal.ModelJsonConverter
import io.medatarun.ext.modeljson.internal.ModelJsonEntityAttributeTypeNotFoundException
import io.medatarun.ext.modeljson.internal.ModelJsonReadEntityReferencedInRelationshipNotFound
import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.BusinessKeyId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.ModelAuthority
import io.medatarun.model.domain.ModelAuthorityIllegalCodeException
import io.medatarun.model.domain.EntityOrigin
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.domain.RelationshipCardinality
import io.medatarun.model.domain.RelationshipCardinalityIllegalCodeException
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.RelationshipRoleId
import io.medatarun.model.domain.TypeId
import io.medatarun.model.domain.TypeKey
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.id.Id
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFails
import kotlin.test.assertFalse

internal class JsonDeserializerV3Test {

    private val converter = ModelJsonConverter(false)

    // -------------------------------------------------------------------------
    // Model read
    // -------------------------------------------------------------------------

    @Test
    fun `model reads with optional fields absent`() {
        // Why: lock mapping for required model root fields.
        val modelId = Id.generate(::ModelId).asString()
        val jsonString = """
            {
              "id":"$modelId",
              "key":"root-fields-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.2.3",
              "authority":"canonical"
            }
        """.trimIndent()

        val modelJson = converter.fromJsonV3(jsonString)
        assertEquals(modelId, modelJson.id.asString())
        assertEquals(ModelKey("root-fields-test"), modelJson.key)
        assertEquals(ModelVersion("1.2.3"), modelJson.version)
        assertEquals(ModelAuthority.CANONICAL, modelJson.authority)
    }

    @Test
    fun `model reads optional documentation home`() {
        // Why: verify documentationHome is read when provided.
        val modelId = Id.generate(::ModelId).asString()
        val docHome = "https://docs.example.org/model"
        val jsonString = """
            {
              "id":"$modelId",
              "key":"root-fields-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.2.3",
              "authority":"canonical",
              "documentationHome":"$docHome"
            }
        """.trimIndent()

        val modelJson = converter.fromJsonV3(jsonString)

        assertEquals(docHome, modelJson.documentationHome?.toString())
    }

    @Test
    fun `model reads tags`() {
        // Why: verify model-level tags mapping.
        val modelId = Id.generate(::ModelId).asString()
        val tagA = Id.generate(::TagId).asString()
        val tagB = Id.generate(::TagId).asString()
        val jsonString = """
            {
              "id":"$modelId",
              "key":"root-fields-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.2.3",
              "authority":"canonical",
              "tags":["$tagA","$tagB"]
            }
        """.trimIndent()

        val modelJson = converter.fromJsonV3(jsonString)

        assertEquals(listOf(tagA, tagB), modelJson.tags.map { it.asString() })
    }

    @Test
    fun `model fails when required root field is missing`() {
        // Why: protect against incomplete root payload.
        val modelId = Id.generate(::ModelId).asString()
        val jsonString = """
            {
              "id":"$modelId",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.2.3",
              "authority":"canonical"
            }
        """.trimIndent()

        val exception = assertThrows<SerializationException>() {
            converter.fromJsonV3(jsonString)
        }

        assertTrue(exception.localizedMessage.contains("'key'"))
    }

    @Test
    fun `model fails when authority is invalid`() {
        // Why: protect model authority enum parsing.
        val modelId = Id.generate(::ModelId).asString()
        val jsonString = """
            {
              "id":"$modelId",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.2.3",
              "key": "mykey",
              "authority":"canonical2"
            }
        """.trimIndent()

        assertThrows<ModelAuthorityIllegalCodeException>() {
            converter.fromJsonV3(jsonString)
        }
    }

    private fun buildModelJson(block: String): String {
        val id = Id.generate(::ModelId)
        return """
            {
              "id":"${id.asString()}",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.2.3",
              "key": "mykey",
              "authority":"canonical",
              $block
            }
        """.trimIndent()
    }

    // -------------------------------------------------------------------------
    // Types read
    // -------------------------------------------------------------------------

    @Test
    fun `types read without name and description`() {
        // Why: verify type optional label fields.
        val typeId = Id.generate(::TypeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)

        assertEquals(1, modelJson.types.size)
        assertEquals(typeId, modelJson.types.first().id.asString())
        assertEquals(TypeKey("text"), modelJson.types.first().key)
        assertNull(modelJson.types.first().name)
        assertNull(modelJson.types.first().description)
    }
    @Test
    fun `types read with name and description`() {
        // Why: verify label fields.
        val typeId = Id.generate(::TypeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text","name":"Text","description":"Text type"}]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)

        assertEquals(1, modelJson.types.size)
        assertEquals(typeId, modelJson.types.first().id.asString())
        assertEquals(TypeKey("text"), modelJson.types.first().key)
        assertEquals("Text", modelJson.types.first().name?.name)
        assertEquals("Text type", modelJson.types.first().description?.name)
    }

    // -------------------------------------------------------------------------
    // Entities read (entities + entity attributes + primary key)
    // -------------------------------------------------------------------------

    @Test
    fun `entities read correctly when optional fields are absent`() {
        // Why: verify entity import succeeds when optional entity fields are absent
        // and still maps required fields (id/key/attributes) correctly.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeId","key":"code","type":"key:text"}
                ]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)
        val entity = modelJson.entities.first()
        val attribute = modelJson.attributes.first()

        assertEquals(entityId, entity.id.asString())
        assertEquals("customer", entity.key.value)
        assertEquals(EntityOrigin.Manual, entity.origin)
        assertEquals(null, entity.name)
        assertEquals(null, entity.description)
        assertEquals(null, entity.documentationHome)
        assertEquals(0, entity.tags.size)

        assertEquals(attributeId, attribute.id.asString())
        assertEquals("code", attribute.key.value)
        assertEquals(false, attribute.optional)
        assertEquals(true, attribute.ownedBy(entity.id))
    }

    @Test
    fun `entities read optional fields when present`() {
        // Why: verify entity optional fields mapping.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val tagId = Id.generate(::TagId).asString()
        val origin = "https://example.org/entities/customer"
        val documentationHome = "https://docs.example.org/entities/customer"
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "name":"Customer",
                "description":"Customer description",
                "origin":"$origin",
                "documentationHome":"$documentationHome",
                "tags":["$tagId"],
                "attributes":[
                  {"id":"$attributeId","key":"code","type":"key:text"}
                ]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)
        val entity = modelJson.entities.first()

        assertEquals(entityId, entity.id.asString())
        assertEquals("customer", entity.key.value)
        assertEquals("Customer", entity.name?.name)
        assertEquals("Customer description", entity.description?.name)
        assertEquals(origin, (entity.origin as EntityOrigin.Uri).uri.toString())
        assertEquals(documentationHome, entity.documentationHome?.toString())
        assertEquals(listOf(tagId), entity.tags.map { it.asString() })
    }

    @Test
    fun `entity attributes read with type key resolution`() {
        // Why: ensure attribute type key resolves to internal type id.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeId","key":"code","type":"key:text"}
                ]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)
        val attribute = modelJson.attributes.first()

        assertEquals(attributeId, attribute.id.asString())
        assertEquals("code", attribute.key.asString())
        assertEquals(typeId, attribute.typeId.asString())
        assertNull(attribute.name)
        assertNull(attribute.description)
        assertFalse(attribute.optional)
        assertTrue(attribute.tags.isEmpty())
    }

    @Test
    fun `entity attributes read optional fields when present`() {
        // Why: verify entity attribute optional fields mapping.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val tagId = Id.generate(::TagId).asString()
        val typeKey = "key:text"
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {
                    "id":"$attributeId",
                    "key":"code",
                    "name":"Code",
                    "description":"Code attribute",
                    "type":"$typeKey",
                    "optional":true,
                    "tags":["$tagId"]
                  }
                ]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)
        val entity = modelJson.entities.first()
        val attribute = modelJson.attributes.first()

        assertEquals(attributeId, attribute.id.asString())
        assertEquals("code", attribute.key.value)
        assertEquals("Code", attribute.name?.name)
        assertEquals("Code attribute", attribute.description?.name)
        assertEquals(true, attribute.optional)
        assertEquals(listOf(tagId), attribute.tags.map { it.asString() })
        assertEquals(typeId, attribute.typeId.asString())
        assertEquals(true, attribute.ownedBy(entity.id))
    }

    @Test
    fun `entity attributes fail when type key is unknown`() {
        // Why: protect type reference resolution.
        val entityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeId","key":"code","type":"key:unknown"}
                ]
              }
            ]
            """.trimIndent()
        )

        assertThrows<ModelJsonEntityAttributeTypeNotFoundException> {
            converter.fromJsonV3(jsonString)
        }
    }

    @Test
    fun `entity primary keys read participants in order`() {
        // Why: lock participant order for entity primary key.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeAId = Id.generate(::AttributeId).asString()
        val attributeBId = Id.generate(::AttributeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeAId","key":"code","type":"key:text"},
                  {"id":"$attributeBId","key":"country","type":"key:text"}
                ],
                "primaryKey":["$attributeAId","$attributeBId"]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)
        val primaryKey = modelJson.entityPrimaryKeys.first()

        assertEquals(entityId, primaryKey.entityId.asString())
        assertEquals(2, primaryKey.participants.size)
        assertEquals(attributeAId, primaryKey.participants[0].attributeId.asString())
        assertEquals(0, primaryKey.participants[0].position)
        assertEquals(attributeBId, primaryKey.participants[1].attributeId.asString())
        assertEquals(1, primaryKey.participants[1].position)
    }

    // -------------------------------------------------------------------------
    // Relationships read (relationships + relationship attributes)
    // -------------------------------------------------------------------------

    @Test
    fun `relationships read with name description and tags`() {
        // Why: lock relationship root fields mapping.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val relationshipId = Id.generate(::RelationshipId).asString()
        val relationshipRoleId = Id.generate(::RelationshipRoleId).asString()
        val relationshipTagId = Id.generate(::TagId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeId","key":"code","type":"key:text"}
                ]
              }
            ],
            "relationships":[
              {
                "id":"$relationshipId",
                "key":"customer_link",
                "name":"Customer Link",
                "description":"Relationship description",
                "tags":["$relationshipTagId"],
                "roles":[
                  {
                    "id":"$relationshipRoleId",
                    "key":"customer",
                    "entity":"key:customer",
                    "cardinality":"one"
                  }
                ]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)
        val relationship = modelJson.relationships.first()

        assertEquals(relationshipId, relationship.id.asString())
        assertEquals("customer_link", relationship.key.value)
        assertEquals("Customer Link", relationship.name?.name)
        assertEquals("Relationship description", relationship.description?.name)
        assertEquals(listOf(relationshipTagId), relationship.tags.map { it.asString() })
    }

    @Test
    fun `relationship roles read with entity key resolution`() {
        // Why: ensure role entity key resolves to internal entity id.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val relationshipRoleId = Id.generate(::RelationshipRoleId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeId","key":"code","type":"key:text"}
                ]
              }
            ],
            "relationships":[
              {
                "key":"customer_link",
                "roles":[
                  {
                    "id":"$relationshipRoleId",
                    "key":"customer_role",
                    "name":"Customer role",
                    "entity":"key:customer",
                    "cardinality":"one"
                  }
                ]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)
        val role = modelJson.relationships.first().roles.first()

        assertEquals(relationshipRoleId, role.id.asString())
        assertEquals("customer_role", role.key.value)
        assertEquals("Customer role", role.name?.name)
        assertEquals(entityId, role.entityId.asString())
        assertEquals(RelationshipCardinality.One, role.cardinality)
    }

    @Test
    fun `relationship roles fail when entity key is unknown`() {
        // Why: protect role entity reference resolution.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeId","key":"code","type":"key:text"}
                ]
              }
            ],
            "relationships":[
              {
                "key":"customer_link",
                "roles":[
                  {
                    "key":"unknown_role",
                    "entity":"key:unknown_entity",
                    "cardinality":"one"
                  }
                ]
              }
            ]
            """.trimIndent()
        )

        assertThrows<ModelJsonReadEntityReferencedInRelationshipNotFound> {
            converter.fromJsonV3(jsonString)
        }
    }

    @Test
    fun `relationship roles fail when cardinality is invalid`() {
        // Why: protect relationship cardinality enum parsing.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeId","key":"code","type":"key:text"}
                ]
              }
            ],
            "relationships":[
              {
                "key":"customer_link",
                "roles":[
                  {
                    "key":"customer_role",
                    "entity":"key:customer",
                    "cardinality":"invalid_cardinality"
                  }
                ]
              }
            ]
            """.trimIndent()
        )

        assertThrows<RelationshipCardinalityIllegalCodeException> {
            converter.fromJsonV3(jsonString)
        }
    }

    @Test
    fun `relationship attributes read`() {
        // Why: verify relationship-owned attributes mapping.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val entityAttributeId = Id.generate(::AttributeId).asString()
        val relationshipId = Id.generate(::RelationshipId).asString()
        val relationshipRoleId = Id.generate(::RelationshipRoleId).asString()
        val relationshipAttributeId = Id.generate(::AttributeId).asString()
        val relationshipAttributeTagId = Id.generate(::TagId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$entityAttributeId","key":"code","type":"key:text"}
                ]
              }
            ],
            "relationships":[
              {
                "id":"$relationshipId",
                "key":"customer_link",
                "roles":[
                  {
                    "id":"$relationshipRoleId",
                    "key":"customer_role",
                    "entity":"key:customer",
                    "cardinality":"one"
                  }
                ],
                "attributes":[
                  {
                    "id":"$relationshipAttributeId",
                    "key":"link_code",
                    "name":"Link Code",
                    "description":"Relationship attribute",
                    "type":"key:text",
                    "optional":true,
                    "tags":["$relationshipAttributeTagId"]
                  }
                ]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)
        val relationshipAttribute = modelJson.attributes.first { it.id.asString() == relationshipAttributeId }

        assertEquals(relationshipAttributeId, relationshipAttribute.id.asString())
        assertEquals("link_code", relationshipAttribute.key.value)
        assertEquals("Link Code", relationshipAttribute.name?.name)
        assertEquals("Relationship attribute", relationshipAttribute.description?.name)
        assertTrue(relationshipAttribute.optional)
        assertEquals(listOf(relationshipAttributeTagId), relationshipAttribute.tags.map { it.asString() })
        assertEquals(typeId, relationshipAttribute.typeId.asString())
        assertTrue(relationshipAttribute.ownedBy(RelationshipId.fromString(relationshipId)))
    }

    @Test
    fun `relationships read multiple without data leak`() {
        // Why: ensure relationship-scoped data stays isolated.
        val typeId = Id.generate(::TypeId).asString()
        val customerEntityId = Id.generate(::EntityId).asString()
        val customerAttributeId = Id.generate(::AttributeId).asString()
        val orderEntityId = Id.generate(::EntityId).asString()
        val orderAttributeId = Id.generate(::AttributeId).asString()
        val firstRelationshipId = Id.generate(::RelationshipId).asString()
        val firstRoleId = Id.generate(::RelationshipRoleId).asString()
        val firstRelationshipTagId = Id.generate(::TagId).asString()
        val firstRelationshipAttributeId = Id.generate(::AttributeId).asString()
        val secondRelationshipId = Id.generate(::RelationshipId).asString()
        val secondRoleId = Id.generate(::RelationshipRoleId).asString()
        val secondRelationshipTagId = Id.generate(::TagId).asString()
        val secondRelationshipAttributeId = Id.generate(::AttributeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$customerEntityId",
                "key":"customer",
                "attributes":[
                  {"id":"$customerAttributeId","key":"customer_code","type":"key:text"}
                ]
              },
              {
                "id":"$orderEntityId",
                "key":"order",
                "attributes":[
                  {"id":"$orderAttributeId","key":"order_code","type":"key:text"}
                ]
              }
            ],
            "relationships":[
              {
                "id":"$firstRelationshipId",
                "key":"customer_order",
                "tags":["$firstRelationshipTagId"],
                "roles":[
                  {
                    "id":"$firstRoleId",
                    "key":"customer_role",
                    "entity":"key:customer",
                    "cardinality":"one"
                  }
                ],
                "attributes":[
                  {"id":"$firstRelationshipAttributeId","key":"customer_ref","type":"key:text"}
                ]
              },
              {
                "id":"$secondRelationshipId",
                "key":"order_customer",
                "tags":["$secondRelationshipTagId"],
                "roles":[
                  {
                    "id":"$secondRoleId",
                    "key":"order_role",
                    "entity":"key:order",
                    "cardinality":"many"
                  }
                ],
                "attributes":[
                  {"id":"$secondRelationshipAttributeId","key":"order_ref","type":"key:text"}
                ]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)
        val firstRelationship = modelJson.relationships.first { it.id.asString() == firstRelationshipId }
        val secondRelationship = modelJson.relationships.first { it.id.asString() == secondRelationshipId }
        val firstRelationshipAttributes = modelJson.attributes.filter { it.ownedBy(firstRelationship.id) }
        val secondRelationshipAttributes = modelJson.attributes.filter { it.ownedBy(secondRelationship.id) }

        assertEquals("customer_role", firstRelationship.roles.first().key.value)
        assertEquals(customerEntityId, firstRelationship.roles.first().entityId.asString())
        assertEquals(listOf(firstRelationshipTagId), firstRelationship.tags.map { it.asString() })
        assertEquals(listOf(firstRelationshipAttributeId), firstRelationshipAttributes.map { it.id.asString() })

        assertEquals("order_role", secondRelationship.roles.first().key.value)
        assertEquals(orderEntityId, secondRelationship.roles.first().entityId.asString())
        assertEquals(listOf(secondRelationshipTagId), secondRelationship.tags.map { it.asString() })
        assertEquals(listOf(secondRelationshipAttributeId), secondRelationshipAttributes.map { it.id.asString() })
    }

    // -------------------------------------------------------------------------
    // Business keys read
    // -------------------------------------------------------------------------

    @Test
    fun `business keys read with all fields`() {
        // Why: lock full business key mapping.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeAId = Id.generate(::AttributeId).asString()
        val attributeBId = Id.generate(::AttributeId).asString()
        val businessKeyId = Id.generate(::BusinessKeyId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeAId","key":"code","type":"key:text"},
                  {"id":"$attributeBId","key":"country","type":"key:text"}
                ]
              }
            ],
            "businessKeys":[
              {
                "id":"$businessKeyId",
                "key":"customer_code_country",
                "entity":"id:$entityId",
                "name":"Customer code-country",
                "description":"Business key description",
                "participants":["id:$attributeAId","id:$attributeBId"]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)
        val businessKey = modelJson.businessKeys.first()

        assertEquals(businessKeyId, businessKey.id.asString())
        assertEquals("customer_code_country", businessKey.key.value)
        assertEquals(entityId, businessKey.entityId.asString())
        assertEquals("Customer code-country", businessKey.name)
        assertEquals("Business key description", businessKey.description)
        assertEquals(attributeAId, businessKey.participants[0].attributeId.asString())
        assertEquals(0, businessKey.participants[0].position)
        assertEquals(attributeBId, businessKey.participants[1].attributeId.asString())
        assertEquals(1, businessKey.participants[1].position)
    }

    @Test
    fun `business keys read without optional fields`() {
        // Why: verify business key optional fields when absent.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeId","key":"code","type":"key:text"}
                ]
              }
            ],
            "businessKeys":[
              {
                "key":"customer_code",
                "entity":"key:customer",
                "participants":["key:code"]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)
        val businessKey = modelJson.businessKeys.first()

        assertEquals("customer_code", businessKey.key.value)
        assertEquals(entityId, businessKey.entityId.asString())
        assertNull(businessKey.name)
        assertNull(businessKey.description)
        assertEquals(attributeId, businessKey.participants.first().attributeId.asString())
    }

    @Test
    fun `business keys read participants in order`() {
        // Why: lock participant order for business keys.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeAId = Id.generate(::AttributeId).asString()
        val attributeBId = Id.generate(::AttributeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeAId","key":"code","type":"key:text"},
                  {"id":"$attributeBId","key":"country","type":"key:text"}
                ]
              }
            ],
            "businessKeys":[
              {
                "key":"customer_country_code",
                "entity":"id:$entityId",
                "participants":["key:country","id:$attributeAId"]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)
        val participants = modelJson.businessKeys.first().participants

        assertEquals(2, participants.size)
        assertEquals(attributeBId, participants[0].attributeId.asString())
        assertEquals(0, participants[0].position)
        assertEquals(attributeAId, participants[1].attributeId.asString())
        assertEquals(1, participants[1].position)
    }

    @Test
    fun `business keys read when field is absent`() {
        // Why: export can omit businessKeys when empty, import must accept it.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeId","key":"code","type":"key:text"}
                ]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)

        assertTrue(modelJson.businessKeys.isEmpty())
    }

    @Test
    fun `business keys fail when entity id is unknown`() {
        // Why: protect business key entity reference resolution.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val unknownEntityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeId","key":"code","type":"key:text"}
                ]
              }
            ],
            "businessKeys":[
              {
                "key":"customer_code",
                "entity":"id:$unknownEntityId",
                "participants":["id:$attributeId"]
              }
            ]
            """.trimIndent()
        )

        assertFails {
            converter.fromJsonV3(jsonString)
        }
    }

    @Test
    fun `business keys fail when participant attribute id is unknown`() {
        // Why: protect business key participant attribute references.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val unknownAttributeId = Id.generate(::AttributeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeId","key":"code","type":"key:text"}
                ]
              }
            ],
            "businessKeys":[
              {
                "key":"customer_code",
                "entity":"id:$entityId",
                "participants":["id:$unknownAttributeId"]
              }
            ]
            """.trimIndent()
        )

        assertFails {
            converter.fromJsonV3(jsonString)
        }
    }
}
