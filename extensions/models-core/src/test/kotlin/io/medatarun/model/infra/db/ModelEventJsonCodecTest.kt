package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.events.ModelEventSystem
import io.medatarun.model.ports.needs.*
import io.medatarun.tags.core.domain.TagId
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URL
import kotlin.reflect.full.primaryConstructor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ModelEventJsonCodecTest {

    private val sys = ModelEventSystem()
    private val codec = sys.codec

    @Test
    fun `encode uses the expected event contract`() {
        val testCases = cmdTestCases()

        for (testCase in testCases) {
            val encoded = codec.encode(testCase.cmd)

            assertEquals(testCase.eventType, encoded.eventType, "Wrong event type for ${testCase.cmd::class.simpleName}")
            assertEquals(testCase.eventVersion, encoded.eventVersion, "Wrong event version for ${testCase.cmd::class.simpleName}")
            assertJsonEquals(testCase.json, encoded.payload, "Wrong payload for ${testCase.cmd::class.simpleName}")
        }
    }

    @Test
    fun `decode reads the expected event contract`() {
        val testCases = cmdTestCases()

        for (testCase in testCases) {
            val decoded = codec.decode(testCase.eventType, testCase.eventVersion, testCase.json)
            assertEquals(testCase.cmd, decoded, "Wrong decoded command for ${testCase.eventType}")
        }
    }

    @Test
    fun `decode unknown event contract throws dedicated exception`() {
        assertFailsWith<ModelRepoCmdEventUnknownContractException> {
            codec.decode("unknown_event", 1, "{}")
        }
    }

    @Test
    fun `model repo commands do not define default constructor values`() {
        val optionalParametersByCommand = ModelStorageCmd::class.sealedSubclasses.mapNotNull { commandClass ->
            val constructor = commandClass.primaryConstructor ?: return@mapNotNull null
            val optionalParameters = constructor.parameters.filter { it.isOptional }.mapNotNull { it.name }
            if (optionalParameters.isEmpty()) {
                null
            } else {
                "${commandClass.simpleName}: ${optionalParameters.joinToString(", ")}"
            }
        }

        assertTrue(
            actual = optionalParametersByCommand.isEmpty(),
            message = "ModelRepoCmd constructors must not define default values. Offenders: ${optionalParametersByCommand.joinToString("; ")}"
        )
    }

    private fun cmdTestCases(): List<CmdTestCase> {
        val modelId = modelId("00000000-0000-0000-0000-000000000001")
        val typeId = typeId("00000000-0000-0000-0000-000000000002")
        val entityId = entityId("00000000-0000-0000-0000-000000000003")
        val relationshipId = relationshipId("00000000-0000-0000-0000-000000000004")
        val relationshipRoleId = relationshipRoleId("00000000-0000-0000-0000-000000000005")
        val entityAttributeId = attributeId("00000000-0000-0000-0000-000000000006")
        val relationshipAttributeId = attributeId("00000000-0000-0000-0000-000000000007")
        val tagId = tagId("00000000-0000-0000-0000-000000000008")

        return listOf(
            CmdTestCase(
                eventType = "model_aggregate_stored",
                eventVersion = 1,
                cmd = sampleStoreModelAggregate(),
                json = """
                    {"model":{"id":"00000000-0000-0000-0000-000000000101","key":"billing","name":"Billing","description":"Billing model","version":"1.0.0","origin":{"origin_type":"manual"},"authority":"system","documentation_home":"https://example.com/docs/models/billing"},"types":[{"id":"00000000-0000-0000-0000-000000000102","key":"number","name":"Number","description":"Number type"}],"entities":[{"id":"00000000-0000-0000-0000-000000000103","key":"invoice","name":"Invoice","description":"Invoice entity","identifier_attribute_id":"00000000-0000-0000-0000-000000000106","origin":{"origin_type":"manual"},"documentation_home":"https://example.com/docs/entities/invoice"}],"entity_attributes":[{"id":"00000000-0000-0000-0000-000000000106","entity_id":"00000000-0000-0000-0000-000000000103","key":"invoice_id","name":"Invoice Id","description":"Invoice identity","type_id":"00000000-0000-0000-0000-000000000102","optional":false}],"relationships":[{"id":"00000000-0000-0000-0000-000000000104","key":"invoice_invoice","name":"Invoice Invoice","description":"Self relationship","roles":[{"id":"00000000-0000-0000-0000-000000000105","key":"source","entity_id":"00000000-0000-0000-0000-000000000103","name":"Source","cardinality":"many"}]}],"relationship_attributes":[{"id":"00000000-0000-0000-0000-000000000107","relationship_id":"00000000-0000-0000-0000-000000000104","key":"ratio","name":"Ratio","description":"Relationship ratio","type_id":"00000000-0000-0000-0000-000000000102","optional":true}]}
                """.trimIndent()
            ),
            CmdTestCase(
                eventType = "model_created",
                eventVersion = 1,
                cmd = ModelStorageCmd.CreateModel(
                    id = modelId,
                    key = ModelKey("crm"),
                    name = text("CRM"),
                    description = markdown("CRM model"),
                    version = ModelVersion("1.0.0"),
                    origin = ModelOrigin.Uri(URI("https://example.com/model/crm")),
                    authority = ModelAuthority.CANONICAL,
                    documentationHome = URL("https://example.com/docs/models/crm")
                ),
                json = """
                    {"id":"00000000-0000-0000-0000-000000000001","key":"crm","name":"CRM","description":"CRM model","version":"1.0.0","origin":{"origin_type":"uri","uri":"https://example.com/model/crm"},"authority":"canonical","documentation_home":"https://example.com/docs/models/crm"}
                """.trimIndent()
            ),
            CmdTestCase(
                eventType = "model_name_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateModelName(modelId, text("CRM")),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","name":"CRM"}"""
            ),
            CmdTestCase(
                eventType = "model_key_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateModelKey(modelId, ModelKey("crm-v2")),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","key":"crm-v2"}"""
            ),
            CmdTestCase(
                eventType = "model_description_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateModelDescription(modelId, markdown("Model description")),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","description":"Model description"}"""
            ),
            CmdTestCase(
                eventType = "model_authority_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateModelAuthority(modelId, ModelAuthority.SYSTEM),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","authority":"system"}"""
            ),
            CmdTestCase(
                eventType = "model_version_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateModelVersion(modelId, ModelVersion("2.0.0")),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","version":"2.0.0"}"""
            ),
            CmdTestCase(
                eventType = "model_documentation_home_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateModelDocumentationHome(modelId, URL("https://example.com/docs/models/crm")),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","url":"https://example.com/docs/models/crm"}"""
            ),
            CmdTestCase(
                eventType = "model_tag_added",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateModelTagAdd(modelId, tagId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","tag_id":"00000000-0000-0000-0000-000000000008"}"""
            ),
            CmdTestCase(
                eventType = "model_tag_deleted",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateModelTagDelete(modelId, tagId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","tag_id":"00000000-0000-0000-0000-000000000008"}"""
            ),
            CmdTestCase(
                eventType = "model_deleted",
                eventVersion = 1,
                cmd = ModelStorageCmd.DeleteModel(modelId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001"}"""
            ),
            CmdTestCase(
                eventType = "type_created",
                eventVersion = 1,
                cmd = ModelStorageCmd.CreateType(
                    modelId = modelId,
                    key = TypeKey("boolean"),
                    name = text("Boolean"),
                    description = markdown("Boolean type")
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","key":"boolean","name":"Boolean","description":"Boolean type"}"""
            ),
            CmdTestCase(
                eventType = "type_key_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateTypeKey(modelId, typeId, TypeKey("string")),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","type_id":"00000000-0000-0000-0000-000000000002","value":"string"}"""
            ),
            CmdTestCase(
                eventType = "type_name_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateTypeName(modelId, typeId, text("String")),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","type_id":"00000000-0000-0000-0000-000000000002","value":"String"}"""
            ),
            CmdTestCase(
                eventType = "type_description_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateTypeDescription(modelId, typeId, markdown("String type")),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","type_id":"00000000-0000-0000-0000-000000000002","value":"String type"}"""
            ),
            CmdTestCase(
                eventType = "type_deleted",
                eventVersion = 1,
                cmd = ModelStorageCmd.DeleteType(modelId, typeId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","type_id":"00000000-0000-0000-0000-000000000002"}"""
            ),
            CmdTestCase(
                eventType = "entity_created",
                eventVersion = 1,
                cmd = ModelStorageCmd.CreateEntity(
                    modelId = modelId,
                    entityId = entityId,
                    key = EntityKey("customer"),
                    name = text("Customer"),
                    description = markdown("Customer entity"),
                    documentationHome = URL("https://example.com/docs/entities/customer"),
                    origin = EntityOrigin.Uri(URI("https://example.com/origin/customer")),
                    identityAttributeId = entityAttributeId,
                    identityAttributeKey = AttributeKey("customer_id"),
                    identityAttributeTypeId = typeId,
                    identityAttributeName = text("Customer Id"),
                    identityAttributeDescription = markdown("Identity attribute"),
                    identityAttributeIdOptional = false
                ),
                json = """
                    {"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","key":"customer","name":"Customer","description":"Customer entity","documentation_home":"https://example.com/docs/entities/customer","origin":{"origin_type":"uri","uri":"https://example.com/origin/customer"},"identity_attribute_id":"00000000-0000-0000-0000-000000000006","identity_attribute_key":"customer_id","identity_attribute_type_id":"00000000-0000-0000-0000-000000000002","identity_attribute_name":"Customer Id","identity_attribute_description":"Identity attribute","identity_attribute_optional":false}
                """.trimIndent()
            ),
            CmdTestCase(
                eventType = "entity_key_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityKey(modelId, entityId, EntityKey("client")),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","value":"client"}"""
            ),
            CmdTestCase(
                eventType = "entity_name_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityName(modelId, entityId, text("Client")),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","value":"Client"}"""
            ),
            CmdTestCase(
                eventType = "entity_description_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityDescription(modelId, entityId, markdown("Client entity")),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","value":"Client entity"}"""
            ),
            CmdTestCase(
                eventType = "entity_identifier_attribute_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityIdentifierAttribute(modelId, entityId, entityAttributeId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","value":"00000000-0000-0000-0000-000000000006"}"""
            ),
            CmdTestCase(
                eventType = "entity_documentation_home_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityDocumentationHome(
                    modelId,
                    entityId,
                    URL("https://example.com/docs/entities/client")
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","value":"https://example.com/docs/entities/client"}"""
            ),
            CmdTestCase(
                eventType = "entity_tag_added",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityTagAdd(modelId, entityId, tagId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","tag_id":"00000000-0000-0000-0000-000000000008"}"""
            ),
            CmdTestCase(
                eventType = "entity_tag_deleted",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityTagDelete(modelId, entityId, tagId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","tag_id":"00000000-0000-0000-0000-000000000008"}"""
            ),
            CmdTestCase(
                eventType = "entity_deleted",
                eventVersion = 1,
                cmd = ModelStorageCmd.DeleteEntity(modelId, entityId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003"}"""
            ),
            CmdTestCase(
                eventType = "entity_attribute_created",
                eventVersion = 1,
                cmd = ModelStorageCmd.CreateEntityAttribute(
                    modelId,
                    entityId,
                    entityAttributeId,
                    AttributeKey("code"),
                    text("Code"),
                    markdown("Entity code"),
                    typeId,
                    false
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","attribute_id":"00000000-0000-0000-0000-000000000006","key":"code","name":"Code","description":"Entity code","type_id":"00000000-0000-0000-0000-000000000002","optional":false}"""
            ),
            CmdTestCase(
                eventType = "entity_attribute_deleted",
                eventVersion = 1,
                cmd = ModelStorageCmd.DeleteEntityAttribute(modelId, entityId, entityAttributeId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","attribute_id":"00000000-0000-0000-0000-000000000006"}"""
            ),
            CmdTestCase(
                eventType = "entity_attribute_key_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityAttributeKey(
                    modelId,
                    entityId,
                    entityAttributeId,
                    AttributeKey("external_code")
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","attribute_id":"00000000-0000-0000-0000-000000000006","value":"external_code"}"""
            ),
            CmdTestCase(
                eventType = "entity_attribute_name_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityAttributeName(
                    modelId,
                    entityId,
                    entityAttributeId,
                    text("External Code")
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","attribute_id":"00000000-0000-0000-0000-000000000006","value":"External Code"}"""
            ),
            CmdTestCase(
                eventType = "entity_attribute_description_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityAttributeDescription(
                    modelId,
                    entityId,
                    entityAttributeId,
                    markdown("External code attribute")
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","attribute_id":"00000000-0000-0000-0000-000000000006","value":"External code attribute"}"""
            ),
            CmdTestCase(
                eventType = "entity_attribute_type_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityAttributeType(modelId, entityId, entityAttributeId, typeId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","attribute_id":"00000000-0000-0000-0000-000000000006","value":"00000000-0000-0000-0000-000000000002"}"""
            ),
            CmdTestCase(
                eventType = "entity_attribute_optional_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityAttributeOptional(modelId, entityId, entityAttributeId, true),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","attribute_id":"00000000-0000-0000-0000-000000000006","value":true}"""
            ),
            CmdTestCase(
                eventType = "entity_attribute_tag_added",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityAttributeTagAdd(modelId, entityId, entityAttributeId, tagId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","attribute_id":"00000000-0000-0000-0000-000000000006","tag_id":"00000000-0000-0000-0000-000000000008"}"""
            ),
            CmdTestCase(
                eventType = "entity_attribute_tag_deleted",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateEntityAttributeTagDelete(modelId, entityId, entityAttributeId, tagId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","entity_id":"00000000-0000-0000-0000-000000000003","attribute_id":"00000000-0000-0000-0000-000000000006","tag_id":"00000000-0000-0000-0000-000000000008"}"""
            ),
            CmdTestCase(
                eventType = "relationship_created",
                eventVersion = 1,
                cmd = ModelStorageCmd.CreateRelationship(
                    modelId = modelId,
                    relationshipId = relationshipId,
                    key = RelationshipKey("customer_order"),
                    name = text("Customer Order"),
                    description = markdown("Customer order relationship"),
                    roles = listOf(
                        ModelStorageCmd.RelationshipRoleInitializer(
                            id = relationshipRoleId,
                            key = RelationshipRoleKey("customer"),
                            entityId = entityId,
                            name = text("Customer"),
                            cardinality = RelationshipCardinality.Many
                        )
                    )
                ),
                json = """
                    {"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","key":"customer_order","name":"Customer Order","description":"Customer order relationship","roles":[{"id":"00000000-0000-0000-0000-000000000005","key":"customer","entity_id":"00000000-0000-0000-0000-000000000003","name":"Customer","cardinality":"many"}]}
                """.trimIndent()
            ),
            CmdTestCase(
                eventType = "relationship_key_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipKey(modelId, relationshipId, RelationshipKey("customer_invoice")),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","value":"customer_invoice"}"""
            ),
            CmdTestCase(
                eventType = "relationship_name_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipName(modelId, relationshipId, text("Customer Invoice")),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","value":"Customer Invoice"}"""
            ),
            CmdTestCase(
                eventType = "relationship_description_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipDescription(
                    modelId,
                    relationshipId,
                    markdown("Customer invoice relationship")
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","value":"Customer invoice relationship"}"""
            ),
            CmdTestCase(
                eventType = "relationship_role_created",
                eventVersion = 1,
                cmd = ModelStorageCmd.CreateRelationshipRole(
                    modelId,
                    relationshipId,
                    relationshipRoleId,
                    RelationshipRoleKey("buyer"),
                    entityId,
                    text("Buyer"),
                    RelationshipCardinality.One
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","relationship_role_id":"00000000-0000-0000-0000-000000000005","key":"buyer","entity_id":"00000000-0000-0000-0000-000000000003","name":"Buyer","cardinality":"one"}"""
            ),
            CmdTestCase(
                eventType = "relationship_role_key_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipRoleKey(
                    modelId,
                    relationshipId,
                    relationshipRoleId,
                    RelationshipRoleKey("seller")
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","relationship_role_id":"00000000-0000-0000-0000-000000000005","value":"seller"}"""
            ),
            CmdTestCase(
                eventType = "relationship_role_name_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipRoleName(
                    modelId,
                    relationshipId,
                    relationshipRoleId,
                    text("Seller")
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","relationship_role_id":"00000000-0000-0000-0000-000000000005","value":"Seller"}"""
            ),
            CmdTestCase(
                eventType = "relationship_role_entity_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipRoleEntity(modelId, relationshipId, relationshipRoleId, entityId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","relationship_role_id":"00000000-0000-0000-0000-000000000005","value":"00000000-0000-0000-0000-000000000003"}"""
            ),
            CmdTestCase(
                eventType = "relationship_role_cardinality_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipRoleCardinality(
                    modelId,
                    relationshipId,
                    relationshipRoleId,
                    RelationshipCardinality.ZeroOrOne
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","relationship_role_id":"00000000-0000-0000-0000-000000000005","value":"zeroOrOne"}"""
            ),
            CmdTestCase(
                eventType = "relationship_tag_added",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipTagAdd(modelId, relationshipId, tagId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","tag_id":"00000000-0000-0000-0000-000000000008"}"""
            ),
            CmdTestCase(
                eventType = "relationship_tag_deleted",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipTagDelete(modelId, relationshipId, tagId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","tag_id":"00000000-0000-0000-0000-000000000008"}"""
            ),
            CmdTestCase(
                eventType = "relationship_deleted",
                eventVersion = 1,
                cmd = ModelStorageCmd.DeleteRelationship(modelId, relationshipId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004"}"""
            ),
            CmdTestCase(
                eventType = "relationship_role_deleted",
                eventVersion = 1,
                cmd = ModelStorageCmd.DeleteRelationshipRole(modelId, relationshipId, relationshipRoleId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","relationship_role_id":"00000000-0000-0000-0000-000000000005"}"""
            ),
            CmdTestCase(
                eventType = "relationship_attribute_created",
                eventVersion = 1,
                cmd = ModelStorageCmd.CreateRelationshipAttribute(
                    modelId,
                    relationshipId,
                    relationshipAttributeId,
                    AttributeKey("weight"),
                    text("Weight"),
                    markdown("Relationship weight"),
                    typeId,
                    true
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","attribute_id":"00000000-0000-0000-0000-000000000007","key":"weight","name":"Weight","description":"Relationship weight","type_id":"00000000-0000-0000-0000-000000000002","optional":true}"""
            ),
            CmdTestCase(
                eventType = "relationship_attribute_name_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipAttributeName(
                    modelId,
                    relationshipId,
                    relationshipAttributeId,
                    text("Load")
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","attribute_id":"00000000-0000-0000-0000-000000000007","value":"Load"}"""
            ),
            CmdTestCase(
                eventType = "relationship_attribute_description_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipAttributeDescription(
                    modelId,
                    relationshipId,
                    relationshipAttributeId,
                    markdown("Relationship load")
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","attribute_id":"00000000-0000-0000-0000-000000000007","value":"Relationship load"}"""
            ),
            CmdTestCase(
                eventType = "relationship_attribute_key_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipAttributeKey(
                    modelId,
                    relationshipId,
                    relationshipAttributeId,
                    AttributeKey("load")
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","attribute_id":"00000000-0000-0000-0000-000000000007","value":"load"}"""
            ),
            CmdTestCase(
                eventType = "relationship_attribute_type_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipAttributeType(
                    modelId,
                    relationshipId,
                    relationshipAttributeId,
                    typeId
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","attribute_id":"00000000-0000-0000-0000-000000000007","value":"00000000-0000-0000-0000-000000000002"}"""
            ),
            CmdTestCase(
                eventType = "relationship_attribute_optional_updated",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipAttributeOptional(
                    modelId,
                    relationshipId,
                    relationshipAttributeId,
                    false
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","attribute_id":"00000000-0000-0000-0000-000000000007","value":false}"""
            ),
            CmdTestCase(
                eventType = "relationship_attribute_tag_added",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipAttributeTagAdd(
                    modelId,
                    relationshipId,
                    relationshipAttributeId,
                    tagId
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","attribute_id":"00000000-0000-0000-0000-000000000007","tag_id":"00000000-0000-0000-0000-000000000008"}"""
            ),
            CmdTestCase(
                eventType = "relationship_attribute_tag_deleted",
                eventVersion = 1,
                cmd = ModelStorageCmd.UpdateRelationshipAttributeTagDelete(
                    modelId,
                    relationshipId,
                    relationshipAttributeId,
                    tagId
                ),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","attribute_id":"00000000-0000-0000-0000-000000000007","tag_id":"00000000-0000-0000-0000-000000000008"}"""
            ),
            CmdTestCase(
                eventType = "relationship_attribute_deleted",
                eventVersion = 1,
                cmd = ModelStorageCmd.DeleteRelationshipAttribute(modelId, relationshipId, relationshipAttributeId),
                json = """{"model_id":"00000000-0000-0000-0000-000000000001","relationship_id":"00000000-0000-0000-0000-000000000004","attribute_id":"00000000-0000-0000-0000-000000000007"}"""
            ),
        )
    }

    private fun sampleStoreModelAggregate(): ModelStorageCmd.StoreModelAggregate {
        val modelId = modelId("00000000-0000-0000-0000-000000000101")
        val typeId = typeId("00000000-0000-0000-0000-000000000102")
        val entityId = entityId("00000000-0000-0000-0000-000000000103")
        val relationshipId = relationshipId("00000000-0000-0000-0000-000000000104")
        val relationshipRoleId = relationshipRoleId("00000000-0000-0000-0000-000000000105")
        val entityAttributeId = attributeId("00000000-0000-0000-0000-000000000106")
        val relationshipAttributeId = attributeId("00000000-0000-0000-0000-000000000107")

        return ModelStorageCmd.StoreModelAggregate(
            model = StoreModelAggregateModel(
                id = modelId,
                key = ModelKey("billing"),
                name = text("Billing"),
                description = markdown("Billing model"),
                version = ModelVersion("1.0.0"),
                origin = ModelOrigin.Manual,
                authority = ModelAuthority.SYSTEM,
                documentationHome = URL("https://example.com/docs/models/billing")
            ),
            types = listOf(
                StoreModelAggregateType(
                    id = typeId,
                    key = TypeKey("number"),
                    name = text("Number"),
                    description = markdown("Number type")
                )
            ),
            entities = listOf(
                StoreModelAggregateEntity(
                    id = entityId,
                    key = EntityKey("invoice"),
                    name = text("Invoice"),
                    description = markdown("Invoice entity"),
                    identifierAttributeId = entityAttributeId,
                    origin = EntityOrigin.Manual,
                    documentationHome = URL("https://example.com/docs/entities/invoice")
                )
            ),
            entityAttributes = listOf(
                StoreModelAggregateEntityAttribute(
                    id = entityAttributeId,
                    entityId = entityId,
                    key = AttributeKey("invoice_id"),
                    name = text("Invoice Id"),
                    description = markdown("Invoice identity"),
                    typeId = typeId,
                    optional = false
                )
            ),
            relationships = listOf(
                StoreModelAggregateRelationship(
                    id = relationshipId,
                    key = RelationshipKey("invoice_invoice"),
                    name = text("Invoice Invoice"),
                    description = markdown("Self relationship"),
                    roles = listOf(
                        StoreModelAggregateRelationshipRole(
                            id = relationshipRoleId,
                            key = RelationshipRoleKey("source"),
                            entityId = entityId,
                            name = text("Source"),
                            cardinality = RelationshipCardinality.Many
                        )
                    )
                )
            ),
            relationshipAttributes = listOf(
                StoreModelAggregateRelationshipAttribute(
                    id = relationshipAttributeId,
                    relationshipId = relationshipId,
                    key = AttributeKey("ratio"),
                    name = text("Ratio"),
                    description = markdown("Relationship ratio"),
                    typeId = typeId,
                    optional = true
                )
            )
        )
    }

    private fun assertJsonEquals(expected: String, actual: String, message: String) {
        assertEquals(
            normalizeJson(expected),
            normalizeJson(actual),
            message
        )
    }

    private fun normalizeJson(value: String): String {
        return Json.parseToJsonElement(value).toString()
    }

    private fun text(value: String): LocalizedText {
        return LocalizedTextNotLocalized(value)
    }

    private fun markdown(value: String): LocalizedMarkdown {
        return LocalizedMarkdownNotLocalized(value)
    }

    private fun modelId(value: String): ModelId {
        return ModelId.fromString(value)
    }

    private fun entityId(value: String): EntityId {
        return EntityId.fromString(value)
    }

    private fun attributeId(value: String): AttributeId {
        return AttributeId.fromString(value)
    }

    private fun relationshipId(value: String): RelationshipId {
        return RelationshipId.fromString(value)
    }

    private fun relationshipRoleId(value: String): RelationshipRoleId {
        return RelationshipRoleId.fromString(value)
    }

    private fun typeId(value: String): TypeId {
        return TypeId.fromString(value)
    }

    private fun tagId(value: String): TagId {
        return TagId.fromString(value)
    }

    data class CmdTestCase(
        val eventType: String,
        val eventVersion: Int,
        val cmd: ModelStorageCmd,
        val json: String
    )
}
