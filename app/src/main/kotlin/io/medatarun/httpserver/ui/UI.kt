package io.medatarun.httpserver.ui

import io.ktor.server.plugins.*
import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.EntityOrigin
import io.medatarun.model.model.ModelId
import io.medatarun.resources.AppResources
import io.medatarun.resources.ResourceInvocationRequest
import io.medatarun.resources.ResourceRepository
import io.medatarun.runtime.AppRuntime
import kotlinx.html.*
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.serialization.json.*
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
    fun renderModelList(): String {
        val data = runtime.modelQueries.findAllModelSummaries()
        return Layout {
            h1 { +"Models" }
            table(classes = "datatable") {
                tbody {
                    for (m in data) {
                        val error = m.error
                        val nameOrId = m.name ?: m.id.value
                        val description = m.description
                        tr {
                            td {
                                a {
                                    href = Links.toModel(m.id)
                                    +nameOrId

                                }
                            }
                            td {
                                div {
                                    style = "display:flex; justify-content:space-between;"
                                    div {
                                        code { +m.id.value }
                                    }
                                    div {
                                        +" "
                                        +("${m.countEntities}×E")
                                        +" "
                                        +("${m.countRelationships}×R")
                                        +" "
                                        +("${m.countTypes}×T")
                                    }
                                }
                                if (description != null) div { +description }
                                if (error != null) {
                                    div { style = "color:red"; +error }
                                }
                            }

                        }
                    }
                }
            }


        }.build()
    }

    fun renderModel(modelId: ModelId): String {
        val model = runtime.modelQueries.findModelById(modelId)
        val id = model.id.value
        val version = model.version.value
        val name = model.name?.name
        val description = model.description?.name
        val documentationHome = model.documentationHome
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
                if (documentationHome!=null) {
                    div { +"Documentation" }
                    div { externalUrl(documentationHome) }
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

    fun commands(body: String?, initialError: String?): String {
        val result: String = when {
            initialError != null -> initialError
            body != null && body.isNotEmpty() -> {
                try {
                    val json = Json.parseToJsonElement(body).jsonObject
                    val action = json["action"]?.jsonPrimitive?.content
                        ?: throw BadRequestException("No action field found")
                    val actionSplit = action.split("/")
                    val resource = actionSplit.getOrNull(0)
                        ?: throw BadRequestException("No resource found in action. Respect the action pattern resource/command")
                    val command = actionSplit.getOrNull(1)
                        ?: throw BadRequestException("No command found in action. Respect the action pattern resource/command")
                    val payload = json["payload"]?.jsonObject ?: buildJsonObject { }
                    resourceRepository.handleInvocation(
                        ResourceInvocationRequest(
                            resource,
                            command,
                            payload
                        )
                    )?.toString() ?: "Success"
                } catch (e: Exception) {
                    e.message ?: "Unknown error"
                }
            }

            else -> ""
        }

        return Layout {
            h1 { +"Commands" }
            form(method = FormMethod.post) {
                input(type = InputType.text, name = "action") {
                    value = body ?: buildJsonObject {
                        put("action", "resource/command")
                        putJsonObject("payload") { }
                    }.toString()
                }
                button {
                    +"Submit"
                }
            }

            if (result.isNotEmpty()) {
                div {
                    style = "border: 1px solid green; padding: 1em;"
                    markdown(result)
                }
            }

            resourceRepository.findAllDescriptors().forEach { resource ->
                h2 { +resource.name }
                div {
                    style =
                        "display: grid; margin-bottom: 1em; grid-template-columns: min-content auto; column-gap: 1em; row-gap:1em;"
                    resource.commands.forEach { command ->
                        div { +command.name }
                        div {
                            div { +(command.description ?: "") }
                            div {
                                style =
                                    "margin-left:2em;display:grid; grid-template-columns: min-content auto; column-gap:1em;"
                                command.parameters.forEach { parameter ->
                                    div {
                                        +parameter.name
                                    }
                                    div {
                                        +parameter.type.toString()
                                    }
                                }
                            }
                        }
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
    span {
        style =
            "font-size: 0.8em; background-color: rgba(255, 255, 255, .5); color: black; padding-left:0.5em; padding-right: 0.5em; border-radius: 0.2em;"
        +str
    }
}

@HtmlTagMarker
fun HtmlBlockTag.externalUrl(url : URL?) {
    if (url == null) return
    a {
        href = url.toExternalForm()
        target="_blank"
        +url.toExternalForm()
    }
}
@HtmlTagMarker
fun HtmlBlockTag.externalUrl(uri : URI?) {
    if (uri == null) return
    a {
        href = uri.normalize().toURL().toExternalForm()
        target="_blank"
        +uri.normalize().toURL().toExternalForm()
    }
}

@Language("css")
val css = """
 
table.datatable td, table.datatable th { vertical-align: top; }
"""