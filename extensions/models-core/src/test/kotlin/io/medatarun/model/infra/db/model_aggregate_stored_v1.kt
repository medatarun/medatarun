package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.ports.needs.*
import org.intellij.lang.annotations.Language
import java.net.URL

val model_aggregate_stored_v1 = ModelEventJsonCodecTest.CmdTestCase(
    eventType = "model_aggregate_stored",
    eventVersion = 1,
    cmd = sampleStoreModelAggregateOld(),
    json = expectedJson(),
    upscaled = expectedUpscale()
)

private fun sampleStoreModelAggregateOld(): ModelStorageCmdOld.StoreModelAggregate {
    val modelId = ModelId.fromString("00000000-0000-0000-0000-000000000101")
    val typeId = TypeId.fromString("00000000-0000-0000-0000-000000000102")
    val entityId = EntityId.fromString("00000000-0000-0000-0000-000000000103")
    val relationshipId = RelationshipId.fromString("00000000-0000-0000-0000-000000000104")
    val relationshipRoleId = RelationshipRoleId.fromString("00000000-0000-0000-0000-000000000105")
    val entityAttributeId = AttributeId.fromString("00000000-0000-0000-0000-000000000106")
    val relationshipAttributeId = AttributeId.fromString("00000000-0000-0000-0000-000000000107")

    return ModelStorageCmdOld.StoreModelAggregate(
        model = StoreModelAggregateModel(
            id = modelId,
            key = ModelKey("billing"),
            name = TextSingleLine("Billing"),
            description = TextMarkdown("Billing model"),
            version = ModelVersion("1.0.0"),
            origin = ModelOrigin.Manual,
            authority = ModelAuthority.SYSTEM,
            documentationHome = URL("https://example.com/docs/models/billing")
        ),
        types = listOf(
            StoreModelAggregateType(
                id = typeId,
                key = TypeKey("number"),
                name = TextSingleLine("Number"),
                description = TextMarkdown("Number type")
            )
        ),
        entities = listOf(
            StoreModelAggregateEntityDeprecated(
                id = entityId,
                key = EntityKey("invoice"),
                name = TextSingleLine("Invoice"),
                description = TextMarkdown("Invoice entity"),
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
                name = TextSingleLine("Invoice Id"),
                description = TextMarkdown("Invoice identity"),
                typeId = typeId,
                optional = false
            )
        ),
        relationships = listOf(
            StoreModelAggregateRelationship(
                id = relationshipId,
                key = RelationshipKey("invoice_invoice"),
                name = TextSingleLine("Invoice Invoice"),
                description = TextMarkdown("Self relationship"),
                roles = listOf(
                    StoreModelAggregateRelationshipRole(
                        id = relationshipRoleId,
                        key = RelationshipRoleKey("source"),
                        entityId = entityId,
                        name = TextSingleLine("Source"),
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
                name = TextSingleLine("Ratio"),
                description = TextMarkdown("Relationship ratio"),
                typeId = typeId,
                optional = true
            )
        )
    )
}


@Language("JSON")
private fun expectedJson() = """
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
      "identifierAttributeId": "00000000-0000-0000-0000-000000000106",
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
  ]
}
""".trimIndent()

fun expectedUpscale(): List<ModelStorageCmd.StoreModelAggregate> {
    val modelId = ModelId.fromString("00000000-0000-0000-0000-000000000101")
    val typeId = TypeId.fromString("00000000-0000-0000-0000-000000000102")
    val entityId = EntityId.fromString("00000000-0000-0000-0000-000000000103")
    val relationshipId = RelationshipId.fromString("00000000-0000-0000-0000-000000000104")
    val relationshipRoleId = RelationshipRoleId.fromString("00000000-0000-0000-0000-000000000105")
    val entityAttributeId = AttributeId.fromString("00000000-0000-0000-0000-000000000106")
    val relationshipAttributeId = AttributeId.fromString("00000000-0000-0000-0000-000000000107")

    val next = ModelStorageCmd.StoreModelAggregate(
        model = StoreModelAggregateModel(
            id = modelId,
            key = ModelKey("billing"),
            name = TextSingleLine("Billing"),
            description = TextMarkdown("Billing model"),
            version = ModelVersion("1.0.0"),
            origin = ModelOrigin.Manual,
            authority = ModelAuthority.SYSTEM,
            documentationHome = URL("https://example.com/docs/models/billing")
        ),
        types = listOf(
            StoreModelAggregateType(
                id = typeId,
                key = TypeKey("number"),
                name = TextSingleLine("Number"),
                description = TextMarkdown("Number type")
            )
        ),
        entities = listOf(
            StoreModelAggregateEntityCurrent(
                id = entityId,
                key = EntityKey("invoice"),
                name = TextSingleLine("Invoice"),
                description = TextMarkdown("Invoice entity"),
                origin = EntityOrigin.Manual,
                documentationHome = URL("https://example.com/docs/entities/invoice")
            )
        ),
        entityAttributes = listOf(
            StoreModelAggregateEntityAttribute(
                id = entityAttributeId,
                entityId = entityId,
                key = AttributeKey("invoice_id"),
                name = TextSingleLine("Invoice Id"),
                description = TextMarkdown("Invoice identity"),
                typeId = typeId,
                optional = false
            )
        ),
        relationships = listOf(
            StoreModelAggregateRelationship(
                id = relationshipId,
                key = RelationshipKey("invoice_invoice"),
                name = TextSingleLine("Invoice Invoice"),
                description = TextMarkdown("Self relationship"),
                roles = listOf(
                    StoreModelAggregateRelationshipRole(
                        id = relationshipRoleId,
                        key = RelationshipRoleKey("source"),
                        entityId = entityId,
                        name = TextSingleLine("Source"),
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
                name = TextSingleLine("Ratio"),
                description = TextMarkdown("Relationship ratio"),
                typeId = typeId,
                optional = true
            )
        ),
        entityPrimaryKeys = listOf(
            StoreModelAggregatePrimaryKey(
                entityId = entityId,
                participants = listOf(entityAttributeId)
            )
        ),
        businessKeys = emptyList()
    )
    return listOf(next)
}