package io.medatarun.httpserver.ui

import io.medatarun.model.model.*
import io.medatarun.resources.AppResources
import io.medatarun.resources.ResourceRepository
import io.medatarun.runtime.AppRuntime
import kotlinx.html.*
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.intellij.lang.annotations.Language
import java.net.URI
import java.net.URL

object Links {
    fun toHome() = "/ui"
    fun toModel(id: ModelId?) = (id?.value ?: "{id}").let { "/ui/model/$it" }
    fun toEntityDef(modelId: ModelId?, entityDefId: EntityDefId?): String {
        val modelIdStr = (modelId?.value ?: "{modelId}")
        val entityDefIdStr = (entityDefId?.value ?: "{entityDefId}")
        return "/ui/model/$modelIdStr/entitydef/$entityDefIdStr"
    }

    fun toCommands() = "/ui/cmd"
}

class UI(private val runtime: AppRuntime) {
    private val resources = AppResources(runtime)
    private val resourceRepository = ResourceRepository(resources)
    fun renderModelListJson(): String {
        val data = runtime.modelQueries.findAllModelSummaries()
        return buildJsonArray {
            data.forEach { m ->
                addJsonObject {
                    put("id", m.id.value)
                    put("name", m.name)
                    put("description", m.description)
                    put("error", m.error)
                    put("countTypes", m.countTypes)
                    put("countEntities", m.countEntities)
                    put("countRelationships", m.countRelationships)
                }
            }
        }.toString()
    }


    fun renderModel(modelId: ModelId): String {
        val model = runtime.modelQueries.findModelById(modelId)
        val id = model.id.value
        val version = model.version.value
        val name = model.name?.name
        val description = model.description?.name
        val documentationHome = model.documentationHome
        val origin = model.origin
        return Layout {
            h1 {
                +"Model "
                +(name ?: id)
            }
            div {
                style = "display:grid; grid-template-columns: min-content auto; column-gap: 1em;"
                div { +"Identifier " }
                div { code { +id } }
                div { +"Version" }
                div { code { +version } }
                if (documentationHome != null) {
                    div { +"Documentation" }
                    div {
                        externalUrl(documentationHome)
                    }
                }
                if (model.hashtags.isNotEmpty()) {
                    div { +"Hashtags" }
                    div { hashtags(model.hashtags) }
                }
                div { +"Origin" }
                div {
                    when (origin) {
                        is ModelOrigin.Manual -> +"Medatarun (manual)"
                        is ModelOrigin.Uri -> externalUrl(origin.uri)
                    }
                }
            }
            if (description != null) markdown(description)


            h2 { +"Entities" }

            ul {
                model.entityDefs.forEach { e ->
                    li {
                        a(href = Links.toEntityDef(model.id, e.id)) {
                            b { +e.id.value }
                        }
                        +" "
                        +(e.name?.name ?: "")
                        +" "
                        markdown(e.description?.name ?: "")
                    }
                }
            }
            h2 { +"Types" }
            ul {
                model.types.forEach { type ->
                    li {
                        b { +type.id.value }
                        +" "
                        +(type.name?.name ?: "")
                        +" "
                        +(type.description?.name ?: "")
                    }
                }

            }

        }.build()
    }

    fun renderEntityDef(modelId: ModelId, entityDefId: EntityDefId): String {
        val model = runtime.modelQueries.findModelById(modelId)
        val e = model.findEntityDef(entityDefId)
        val id = e.id.value
        val name = e.name?.name
        val description = e.description?.name
        val origin = e.origin
        val documentationHome = e.documentationHome
        return Layout {
            h1 {
                +"Entity "
                +(name ?: id)
            }
            div {
                style =
                    "display: grid; grid-template-columns: auto auto; justify-content: start; column-gap: 1em; margin-bottom: 1em;"
                div { +"Identifier" }
                div { code { +id } }
                div { +"Model" }
                div { a(href = Links.toModel(model.id)) { +(model.name?.name ?: model.id.value) } }
                if (documentationHome != null) {
                    div { +"Documentation" }
                    div { externalUrl(documentationHome) }
                }
                if (e.hashtags.isNotEmpty()) {
                    div { +"Hashtags" }
                    div { hashtags(e.hashtags) }
                }
                div { +"Origin" }
                div {
                    when (origin) {
                        EntityOrigin.Manual -> +"Medatarun (manual)"
                        is EntityOrigin.Uri -> externalUrl(origin.uri)
                    }
                }

            }


            if (description != null) div { +description }
            h2 { +"Attributes" }
            ul {
                e.attributes.forEach { attr ->
                    li {
                        +(attr.name?.name ?: "")
                        +" "
                        code {
                            +attr.id.value
                            +":"
                            +attr.type.value
                        }
                        +" "
                        if (attr.optional) {
                            tag("optional")
                        }
                        if (attr.id == e.identifierAttributeDefId) {
                            +" 🔑"
                        }
                        br
                        markdown(attr.description?.name ?: "")
                    }
                }
            }
        }.build()
    }
}

class Layout(val builder: HtmlBlockTag.() -> Unit) {
    fun build(): String {
        return createHTMLDocument().html {
            head {
                title { +"Medatarun" }
                styleLink(url = "https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css")
                style { unsafe { +css } }
            }
            body {
                nav {
                    style = "display: flex; gap: 1em; justify-content: center;"
                    div { +"Medatarun" }
                    div { a(Links.toHome()) { +"🏠" } }
                    div { a(Links.toHome()) { +"Models" } }
                    div { a(Links.toCommands()) { +"Commands" } }

                }
                main(classes = "container") {
                    builder()
                }

            }
        }.serialize()
    }
}

private val parser = Parser.builder().build()

@HtmlTagMarker
fun HtmlBlockTag.markdown(markdownText: String) {
    val document = parser.parse(markdownText)
    val renderer = HtmlRenderer.builder().build()
    val html = renderer.render(document)
    div {
        unsafe { +html }
    }
}

@HtmlTagMarker
fun HtmlBlockTag.tag(str: String) {
    span(classes = "tag") { +str }
}

@HtmlTagMarker
fun HtmlBlockTag.externalUrl(url: URL?) {
    if (url == null) return
    a {
        href = url.toExternalForm()
        target = "_blank"
        +url.toExternalForm()
    }
}

@HtmlTagMarker
fun HtmlBlockTag.externalUrl(uri: URI?) {
    if (uri == null) return
    a {
        href = uri.normalize().toURL().toExternalForm()
        target = "_blank"
        +uri.normalize().toURL().toExternalForm()
    }
}

@HtmlTagMarker
fun HtmlBlockTag.hashtags(hashtags: List<Hashtag>) {
    if (hashtags.isEmpty()) return
    hashtags.forEach { hashtag ->
        span(classes = "tag") {
            style = "margin-right:1em;"
            +hashtag.value
        }
    }
}

@Language("css")
val css = """
 
table.datatable td, table.datatable th { vertical-align: top; }
.tag {
    font-size: 0.8em; 
    background-color: rgba(255, 255, 255, .5); 
    color: black; 
    padding-left:0.5em;
    padding-right: 0.5em;
    border-radius: 0.2em;
}
"""

