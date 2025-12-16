package io.medatarun.ext.frictionlessdata

import io.medatarun.kernel.ResourceLocator
import io.medatarun.model.infra.ModelHumanPrinterEmoji
import org.junit.jupiter.api.Test
import java.net.URI

class TableSchemaTest {

    val conv = FrictionlessConverter()

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
            "https://raw.githubusercontent.com/betagouv/schema-projet-collectivites-transition-ecologique/refs/heads/main/datapackage.json" to "/datapackage-transition-ecologique.json",
            "https://raw.githubusercontent.com/betagouv/schema-projet-collectivites-transition-ecologique/refs/heads/main/projets-territoire/schema.json" to "/datapackage-transition-ecologique-projets-territoir.json",
            "https://raw.githubusercontent.com/betagouv/schema-projet-collectivites-transition-ecologique/refs/heads/main/collectivites/schema.json" to "/datapackage-transition-ecologique-collectivites.json",
        )


        private fun getURIContent(uri: URI): String {
            if (uri.scheme == "classpath") {
                val absoluteUri = resolveUri(uri)
                return this::class.java.getResource(absoluteUri.path).readText()
            } else {
                val name = map[baseURI.toString()]
                    ?: throw IllegalArgumentException(baseURI.toString() + " is not in the map")
                return this::class.java.getResource(name).readText()
            }
        }

        override fun getRootContent(): String {
            return getURIContent(baseURI)
        }

        override fun getContent(path: String): String {
            return getURIContent(resolveUri(path))
        }

        override fun withPath(path: String): ResourceLocator {
            val url = resolveUri(path)
            return TestResourceLocatorURL(url)
        }

        override fun resolveUri(path: String): URI {
            val candidate = URI(path)
            val url = if (candidate.isAbsolute) candidate else baseURI.resolve(candidate)
            return url
        }

        override fun resolveUri(path: URI): URI {
            val url = if (path.isAbsolute) path else baseURI.resolve(path)
            return url
        }
    }

    @Test
    fun test() {
        val resource = "/budget.json"
        val rl = TestResourceLocatorURL(URI("classpath:$resource"))
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

    @Test
    fun testDataPackage2() {
        val resource =
            URI("https://raw.githubusercontent.com/betagouv/schema-projet-collectivites-transition-ecologique/refs/heads/main/datapackage.json")
        val locator = TestResourceLocatorURL(resource)
        val model = conv.readString(resource.toString(), locator)
        println(ModelHumanPrinterEmoji().print(model))
        TODO("Complete tests on DataPackage")
    }
}