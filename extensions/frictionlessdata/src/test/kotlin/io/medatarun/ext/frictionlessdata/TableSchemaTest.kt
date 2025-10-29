package io.medatarun.ext.frictionlessdata

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelHumanPrinterEmoji
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.model.AttributeDefId
import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.LocalizedTextNotLocalized
import io.medatarun.model.model.ModelHumanPrinter
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelTypeId
import io.medatarun.model.model.ModelVersion
import org.junit.jupiter.api.Test

class TableSchemaTest {

    val conv = FrictionlessConverter()

    @Test
    fun test() {
        val resource = "/deliberations.json"
        val model = conv.readString(resource)
        println(ModelHumanPrinterEmoji().print(model))
    }
}