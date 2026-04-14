package io.medatarun.model.infra.db

import io.medatarun.model.domain.BusinessKeyId
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelId
import io.medatarun.model.infra.db.ModelEventJsonCodecTest.CmdTestCase
import io.medatarun.model.ports.needs.ModelStorageCmd.BusinessKeyUpdateName
import org.intellij.lang.annotations.Language

private val modelId = ModelId.generate()
private val businessKeyId = BusinessKeyId.generate()

val business_key_name_updated_v1 = CmdTestCase(
    eventType = "business_key_name_updated",
    eventVersion = 1,
    cmd = BusinessKeyUpdateName(
        modelId = modelId,
        businessKeyId = businessKeyId,
        name = LocalizedTextNotLocalized("ERP Invoice V2")
    ),
    json = json()
)

@Language("JSON")
private fun json(): String =
    """{
  "modelId": "${modelId.value}",
  "businessKeyId": "${businessKeyId.value}",
  "name": "ERP Invoice V2"
}"""

