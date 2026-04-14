package io.medatarun.model.infra.db

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.BusinessKeyId
import io.medatarun.model.domain.ModelId
import io.medatarun.model.infra.db.ModelEventJsonCodecTest.CmdTestCase
import io.medatarun.model.ports.needs.ModelStorageCmd.BusinessKeyUpdateParticipants
import org.intellij.lang.annotations.Language

private val modelId = ModelId.generate()
private val businessKeyId = BusinessKeyId.generate()
private val participantAttributeId1 = AttributeId.generate()
private val participantAttributeId2 = AttributeId.generate()

val business_key_participants_updated_v1 = CmdTestCase(
    eventType = "business_key_participants_updated",
    eventVersion = 1,
    cmd = BusinessKeyUpdateParticipants(
        modelId = modelId,
        businessKeyId = businessKeyId,
        participantAttributeIds = listOf(participantAttributeId1, participantAttributeId2)
    ),
    json = json()
)

@Language("JSON")
private fun json(): String = """{
  "modelId": "${modelId.value}",
  "businessKeyId": "${businessKeyId.value}",
  "participantAttributeIds": [
    "${participantAttributeId1.value}",
    "${participantAttributeId2.value}"
  ]
}"""
