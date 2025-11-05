package io.medatarun.ext.frictionlessdata

import io.medatarun.model.infra.ModelHumanPrinterEmoji
import io.medatarun.model.ports.ResourceLocator
import org.junit.jupiter.api.Test
import org.slf4j.ILoggerFactory
import org.slf4j.LoggerFactory
import java.net.URI

class TableSchemaTest {

    val conv = FrictionlessConverter()

    /**
     * Resource locator for tests that map file paths to test resources.
     */
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

    /**
     * Fake resource locator to map URLs to files in test resources to avoid getting files on internet during tests.
     * This allows tests on datapackage to simulate content fetching on a limited set on URLs
     */
    class TestResourceLocatorURL(val baseURI: URI) : ResourceLocator {


        val map = mapOf(
            "https://raw.githubusercontent.com/cnigfr/schema-paysage/refs/heads/main/datapackage.json" to "/datapackage-paysage.json",
            "https://raw.githubusercontent.com/cnigfr/schema-paysage/refs/heads/main/schema/classe-atlaspaysager/schema.json" to "/datapackage-paysage-classe-atlaspaysager.json",
            "https://raw.githubusercontent.com/cnigfr/schema-paysage/refs/heads/main/schema/classe-documentpaysage/schema.json" to "/datapackage-paysage-classe-documentpaysage.json",
            "https://raw.githubusercontent.com/cnigfr/schema-paysage/refs/heads/main/schema/classe-dynamique/schema.json" to "/datapackage-paysage-classe-dynamique.json",
            "https://raw.githubusercontent.com/cnigfr/schema-paysage/refs/heads/main/schema/classe-ensemblepaysager/schema.json" to "/datapackage-paysage-classe-ensemblepaysager.json",
            "https://raw.githubusercontent.com/cnigfr/schema-paysage/refs/heads/main/schema/classe-limitedecoupagepaysager/schema.json" to "/datapackage-paysage-classe-limitedecoupagepaysager.json",
            "https://raw.githubusercontent.com/cnigfr/schema-paysage/refs/heads/main/schema/classe-sousunitepaysagere/schema.json" to "/datapackage-paysage-classe-sousunitepaysagere.json",
            "https://raw.githubusercontent.com/cnigfr/schema-paysage/refs/heads/main/schema/classe-unitepaysagere/schema.json" to "/datapackage-paysage-classe-unitepaysagere.json",
        )


        override fun getRootContent(): String {
            return this::class.java.getResource(map[baseURI.toString()]).readText()
        }

        override fun getContent(path: String): String {
            val candidate = URI(path)
            val url = if (candidate.isAbsolute) candidate else baseURI.resolve(candidate)
            val resource = map[url.toString()].toString()
            return this::class.java.getResource(resource).readText()
        }

        override fun withPath(path: String): ResourceLocator {
            val candidate = URI(path)
            val url = if (candidate.isAbsolute) candidate else baseURI.resolve(candidate)
            return TestResourceLocatorURL(url)
        }

    }

    @Test
    fun test() {
        val resource = "/budget.json"
        val rl = TestResourceLocator(resource)
        val model = conv.readString(resource, rl)
        println(ModelHumanPrinterEmoji().print(model))
        TODO("Complete tests on tableSchema")
    }

    @Test
    fun testDataPackage() {
        val resource = URI("https://raw.githubusercontent.com/cnigfr/schema-paysage/refs/heads/main/datapackage.json")
        val locator = TestResourceLocatorURL(resource)
        val model = conv.readString(resource.toString(), locator)
        println(ModelHumanPrinterEmoji().print(model))
        TODO("Complete tests on DataPackage")
    }
}