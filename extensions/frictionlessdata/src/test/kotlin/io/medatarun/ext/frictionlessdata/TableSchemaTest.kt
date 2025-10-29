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
import io.medatarun.model.ports.ResourceLocator
import org.junit.jupiter.api.Test

class TableSchemaTest {

    val conv = FrictionlessConverter()
    class TestResourceLocator(val path: String) : ResourceLocator {
        override fun getRootContent(): String {
            return this::class.java.getResource(path).readText()
        }

        override fun getContent(path: String): String {
            TODO("Not yet implemented")
        }

        override fun withPath(path: String): ResourceLocator {
            return TestResourceLocator(path)
        }

    }
    @Test
    fun test() {
        val resource = "/datapackage-paysage.json"
        val rl = TestResourceLocator(resource)
        val model = conv.readString(resource, rl)
        println(ModelHumanPrinterEmoji().print(model))
    }
}