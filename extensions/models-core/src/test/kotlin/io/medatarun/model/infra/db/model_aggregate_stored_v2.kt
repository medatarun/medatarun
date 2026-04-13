package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.ModelEventJsonCodecTest.CmdTestCase
import io.medatarun.model.ports.needs.*
import java.net.URL

val model_aggregate_stored_v2 = CmdTestCase(
    eventType = "model_aggregate_stored",
    eventVersion = 2,
    cmd = sampleStoreModelAggregateCurrent(),
    json = """
                    {
  "model": {
    "id": "00000000-0000-0000-0000-000000000101",
    "key": "billing",
    "name": "Billing",
    "description": "Billing model",
    "version": "1.0.0",
    "origin": {
      "origin_type": "manual"
    },
    "authority": "system",
    "documentationHome": "https://example.com/docs/models/billing"
  },
  "types": [
    {
      "id": "00000000-0000-0000-0000-000000000102",
      "key": "number",
      "name": "Number",
      "description": "Number type"
    }
  ],
  "entities": [
    {
      "id": "00000000-0000-0000-0000-000000000103",
      "key": "invoice",
      "name": "Invoice",
      "description": "Invoice entity",
      "origin": {
        "origin_type": "manual"
      },
      "documentationHome": "https://example.com/docs/entities/invoice"
    }
  ],
  "entityAttributes": [
    {
      "id": "00000000-0000-0000-0000-000000000106",
      "entityId": "00000000-0000-0000-0000-000000000103",
      "key": "invoice_id",
      "name": "Invoice Id",
      "description": "Invoice identity",
      "typeId": "00000000-0000-0000-0000-000000000102",
      "optional": false
    },
    {
      "id": "019d860f-2ff4-7bd7-8317-ad8d192a336a",
      "entityId": "00000000-0000-0000-0000-000000000103",
      "key": "erp_id",
      "name": "ERP Id",
      "description": "Invoice ERP identifier",
      "typeId": "00000000-0000-0000-0000-000000000102",
      "optional": false
    }
  ],
  "relationships": [
    {
      "id": "00000000-0000-0000-0000-000000000104",
      "key": "invoice_invoice",
      "name": "Invoice Invoice",
      "description": "Self relationship",
      "roles": [
        {
          "id": "00000000-0000-0000-0000-000000000105",
          "key": "source",
          "entityId": "00000000-0000-0000-0000-000000000103",
          "name": "Source",
          "cardinality": "many"
        }
      ]
    }
  ],
  "relationshipAttributes": [
    {
      "id": "00000000-0000-0000-0000-000000000107",
      "relationshipId": "00000000-0000-0000-0000-000000000104",
      "key": "ratio",
      "name": "Ratio",
      "description": "Relationship ratio",
      "typeId": "00000000-0000-0000-0000-000000000102",
      "optional": true
    }
  ],
  "entityPrimaryKeys": [
    {
      "entityId": "00000000-0000-0000-0000-000000000103",
      "participants": ["00000000-0000-0000-0000-000000000106"]
    }
  ],
  "businessKeys": [
    {
      "businessKeyId": "019d8610-082d-781c-b36d-2637de81bff9",
      "entityId": "00000000-0000-0000-0000-000000000103",
      "key": "bk",
      "name": "Invoice ERP Key",
      "description": "Invoice number stored in ERP",
      "participants": ["019d860f-2ff4-7bd7-8317-ad8d192a336a"]
    }
  ]
}
                """.trimIndent()
)

private fun sampleStoreModelAggregateCurrent(): ModelStorageCmd.StoreModelAggregate {
    val modelId = ModelId.fromString("00000000-0000-0000-0000-000000000101")
    val typeId = TypeId.fromString("00000000-0000-0000-0000-000000000102")
    val entityId = EntityId.fromString("00000000-0000-0000-0000-000000000103")
    val relationshipId = RelationshipId.fromString("00000000-0000-0000-0000-000000000104")
    val relationshipRoleId = RelationshipRoleId.fromString("00000000-0000-0000-0000-000000000105")
    val entityAttributeId = AttributeId.fromString("00000000-0000-0000-0000-000000000106")
    val entityAttributeErpId = AttributeId.fromString("019d860f-2ff4-7bd7-8317-ad8d192a336a")
    val businessKeyId = BusinessKeyId.fromString("019d8610-082d-781c-b36d-2637de81bff9")
    val relationshipAttributeId = AttributeId.fromString("00000000-0000-0000-0000-000000000107")

    return ModelStorageCmd.StoreModelAggregate(
        model = StoreModelAggregateModel(
            id = modelId,
            key = ModelKey("billing"),
            name = LocalizedTextNotLocalized("Billing"),
            description = LocalizedMarkdownNotLocalized("Billing model"),
            version = ModelVersion("1.0.0"),
            origin = ModelOrigin.Manual,
            authority = ModelAuthority.SYSTEM,
            documentationHome = URL("https://example.com/docs/models/billing")
        ),
        types = listOf(
            StoreModelAggregateType(
                id = typeId,
                key = TypeKey("number"),
                name = LocalizedTextNotLocalized("Number"),
                description = LocalizedMarkdownNotLocalized("Number type")
            )
        ),
        entities = listOf(
            StoreModelAggregateEntityCurrent(
                id = entityId,
                key = EntityKey("invoice"),
                name = LocalizedTextNotLocalized("Invoice"),
                description = LocalizedMarkdownNotLocalized("Invoice entity"),
                origin = EntityOrigin.Manual,
                documentationHome = URL("https://example.com/docs/entities/invoice")
            )
        ),
        entityAttributes = listOf(
            StoreModelAggregateEntityAttribute(
                id = entityAttributeId,
                entityId = entityId,
                key = AttributeKey("invoice_id"),
                name = LocalizedTextNotLocalized("Invoice Id"),
                description = LocalizedMarkdownNotLocalized("Invoice identity"),
                typeId = typeId,
                optional = false
            ),
            StoreModelAggregateEntityAttribute(
                id = entityAttributeErpId,
                entityId = entityId,
                key = AttributeKey("erp_id"),
                name = LocalizedTextNotLocalized("ERP Id"),
                description = LocalizedMarkdownNotLocalized("Invoice ERP identifier"),
                typeId = typeId,
                optional = false
            )
        ),
        relationships = listOf(
            StoreModelAggregateRelationship(
                id = relationshipId,
                key = RelationshipKey("invoice_invoice"),
                name = LocalizedTextNotLocalized("Invoice Invoice"),
                description = LocalizedMarkdownNotLocalized("Self relationship"),
                roles = listOf(
                    StoreModelAggregateRelationshipRole(
                        id = relationshipRoleId,
                        key = RelationshipRoleKey("source"),
                        entityId = entityId,
                        name = LocalizedTextNotLocalized("Source"),
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
                name = LocalizedTextNotLocalized("Ratio"),
                description = LocalizedMarkdownNotLocalized("Relationship ratio"),
                typeId = typeId,
                optional = true
            )
        ),
        entityPrimaryKeys = listOf(
            StoreModelAggregatePrimaryKey(entityId, listOf(entityAttributeId))
        ),
        businessKeys = listOf(
            StoreModelAggregateBusinessKey(
                businessKeyId = businessKeyId,
                entityId = entityId,
                key = BusinessKeyKey("bk"),
                name = "Invoice ERP Key",
                description = "Invoice number stored in ERP",
                participants = listOf(entityAttributeErpId)
            )
        ),
    )
}

