package io.medatarun.model.infra.db

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.BusinessKeyId
import io.medatarun.model.domain.BusinessKeyKey
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelId
import io.medatarun.model.infra.db.ModelEventJsonCodecTest.CmdTestCase
import io.medatarun.model.ports.needs.ModelStorageCmd.BusinessKeyCreate
import org.intellij.lang.annotations.Language

private val modelId = ModelId.generate()
private val entityId = EntityId.generate()
private val businessKeyId = BusinessKeyId.generate()
private val participantAttributeId1 = AttributeId.generate()
private val participantAttributeId2 = AttributeId.generate()

val business_key_created_v1 = CmdTestCase(
    eventType = "business_key_created",
    eventVersion = 1,
    cmd = sample(),
    json = json()
)

private fun sample() = BusinessKeyCreate(
    modelId = modelId,
    entityId = entityId,
    businessKeyId = businessKeyId,
    key = BusinessKeyKey("erp_invoice"),
    name = LocalizedText("ERP Invoice"),
    description = LocalizedMarkdown("ERP invoice key"),
    participantAttributeIds = listOf(participantAttributeId1, participantAttributeId2)
)

@Language("JSON")
private fun json() = """{
  "modelId": "${modelId.value}",
  "entityId": "${entityId.value}",
  "businessKeyId": "${businessKeyId.value}",
  "key": "erp_invoice",
  "name": "ERP Invoice",
  "description": "ERP invoice key",
  "participantAttributeIds": [
    "${participantAttributeId1.value}",
    "${participantAttributeId2.value}"
  ]
}"""