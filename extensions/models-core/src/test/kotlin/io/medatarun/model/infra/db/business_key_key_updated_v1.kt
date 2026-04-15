package io.medatarun.model.infra.db

import io.medatarun.model.domain.BusinessKeyId
import io.medatarun.model.domain.BusinessKeyKey
import io.medatarun.model.domain.ModelId
import io.medatarun.model.infra.db.ModelEventJsonCodecTest.CmdTestCase
import io.medatarun.model.ports.needs.ModelStorageCmd.BusinessKeyUpdateKey
import org.intellij.lang.annotations.Language

private val modelId = ModelId.generate()
private val businessKeyId = BusinessKeyId.generate()

val business_key_key_updated_v1 = CmdTestCase(
    eventType = "business_key_key_updated",
    eventVersion = 1,
    cmd = BusinessKeyUpdateKey(
        modelId = modelId,
        businessKeyId = businessKeyId,
        key = BusinessKeyKey("erp_invoice_v2")
    ),
    json = json()
)


@Language("JSON")
private fun json(): String = """{
  "modelId": "${modelId.value}",
  "businessKeyId": "${businessKeyId.value}",
  "key": "erp_invoice_v2"
}"""