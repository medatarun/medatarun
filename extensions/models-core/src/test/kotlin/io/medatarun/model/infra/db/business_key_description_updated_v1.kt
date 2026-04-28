package io.medatarun.model.infra.db

import io.medatarun.model.domain.BusinessKeyId
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.ModelId
import io.medatarun.model.infra.db.ModelEventJsonCodecTest.CmdTestCase
import io.medatarun.model.ports.needs.ModelStorageCmd.BusinessKeyUpdateDescription
import org.intellij.lang.annotations.Language

private val modelId = ModelId.generate()
private val businessKeyId = BusinessKeyId.generate()

val business_key_description_updated_v1 = CmdTestCase(
    eventType = "business_key_description_updated",
    eventVersion = 1,
    cmd = BusinessKeyUpdateDescription(
        modelId = modelId,
        businessKeyId = businessKeyId,
        description = LocalizedMarkdown("ERP invoice key v2")
    ),
    json = json()
)

@Language("JSON")
private fun json(): String =
    """{
  "modelId": "${modelId.value}",
  "businessKeyId": "${businessKeyId.value}",
  "description": "ERP invoice key v2"
}"""
