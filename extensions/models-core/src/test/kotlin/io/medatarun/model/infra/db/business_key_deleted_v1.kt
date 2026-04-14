package io.medatarun.model.infra.db

import io.medatarun.model.domain.BusinessKeyId
import io.medatarun.model.domain.ModelId
import io.medatarun.model.infra.db.ModelEventJsonCodecTest.CmdTestCase
import io.medatarun.model.ports.needs.ModelStorageCmd.BusinessKeyDelete
import org.intellij.lang.annotations.Language

private val modelId = ModelId.generate()
private val businessKeyId = BusinessKeyId.generate()

val business_key_deleted_v1 = CmdTestCase(
    eventType = "business_key_deleted",
    eventVersion = 1,
    cmd = BusinessKeyDelete(
        modelId = modelId,
        businessKeyId = businessKeyId
    ),
    json = json()
)


@Language("JSON")
private fun json(): String = """{
  "modelId": "${modelId.value}",
  "businessKeyId": "${businessKeyId.value}"
}"""
