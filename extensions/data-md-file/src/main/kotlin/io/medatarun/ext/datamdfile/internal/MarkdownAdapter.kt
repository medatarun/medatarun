package io.medatarun.ext.datamdfile.internal

import io.medatarun.model.model.AttributeDefId
import io.medatarun.model.model.EntityDef
import org.commonmark.Extension
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor
import org.commonmark.node.BulletList
import org.commonmark.node.Heading
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import org.commonmark.renderer.markdown.MarkdownRenderer

/**
 * Handles Markdown serialization/deserialization of entities.
 */
internal class MarkdownAdapter {

    private val markdownExtensions: List<Extension> = listOf(YamlFrontMatterExtension.create())
    private val parser: Parser = Parser.builder().extensions(markdownExtensions).build()
    private val renderer: MarkdownRenderer = MarkdownRenderer.builder().build()

    /**
     * Converts [EntityMarkdownMutable] to [MarkdownString]
     */
    fun toMarkdownString(
        entityDef: EntityDef,
        entity: EntityMarkdownMutable
    ): MarkdownString {
        val frontmatterLines = buildFrontmatterLines(entityDef, entity.attributes)
        val bodyContent = buildBodyContent(entityDef, entity.attributes)

        val str = buildString {
            appendLine("---")
            frontmatterLines.forEach { appendLine(it) }
            appendLine("---")
            if (bodyContent.isNotBlank()) {
                append(bodyContent)
                if (!bodyContent.endsWith("\n")) {
                    append("\n")
                }
            }
        }

        return MarkdownString(str)
    }
    /**
     * Converts [MarkdownString] to [EntityMarkdownMutable]
     */
    fun toEntityMarkdown(
        entityDef: EntityDef,
        content: MarkdownString
    ): EntityMarkdownMutable {
        val document = parser.parse(content.value)

        val frontmatterVisitor = YamlFrontMatterVisitor()
        document.accept(frontmatterVisitor)
        val frontmatterValues = frontmatterVisitor.data.mapValues { entry ->
            entry.value.firstOrNull()
        }

        val bodyValues = parseBodySections(document)

        val values = mutableMapOf<AttributeDefId, Any?>()
        entityDef.attributes.forEach { attribute ->
            val attributeId = attribute.id
            val value = if (attribute.type.value == MARKDOWN_TYPE) {
                bodyValues[attributeId.value]
            } else {
                frontmatterValues[attributeId.value]?.let { if (it == NULL_LITERAL) null else it }
            }
            values[attributeId] = value
        }

        val entityIdValue = values[entityDef.entityIdAttributeDefId()]?.toString()
            ?: throw MdFileEntityIdMissingException(entityDef.id)

        return EntityMarkdownMutable(
            id = EntityInstanceIdString(entityIdValue),
            entityDefId = entityDef.id,
            attributes = values
                .filterValues { it != null }
                .mapValues { it.value as Any }
                .mapKeys { AttributeDefId(it.key.value) }
                .toMutableMap()
        )
    }

    private fun buildFrontmatterLines(
        entityDef: EntityDef,
        values: Map<AttributeDefId, Any?>
    ): List<String> {
        return entityDef.attributes
            .filter { it.type.value != MARKDOWN_TYPE }
            .map { attribute ->
                val value = formatFrontmatterValue(values[attribute.id])
                "${attribute.id.value}: $value"
            }
    }

    private fun buildBodyContent(
        entityDef: EntityDef,
        values: Map<AttributeDefId, Any?>
    ): String {
        val sections = entityDef.attributes
            .filter { it.type.value == MARKDOWN_TYPE }
            .mapNotNull { attribute ->
                val rawValue = values[attribute.id] ?: return@mapNotNull null
                val textValue = rawValue.toString().trimEnd()
                if (textValue.isEmpty()) {
                    null
                } else {
                    buildString {
                        append("## ${attribute.id.value}\n")
                        append(textValue)
                        if (!textValue.endsWith("\n")) {
                            append("\n")
                        }
                    }
                }
            }

        return sections.joinToString(separator = "\n").trimEnd()
    }

    private fun parseBodySections(document: Node): Map<String, String> {
        val result = mutableMapOf<String, String>()
        var currentNode: Node? = document.firstChild
        while (currentNode != null) {
            if (currentNode is Heading && currentNode.level == 2) {
                val key = collectHeadingText(currentNode)
                val sectionBuilder = StringBuilder()
                var contentNode = currentNode.next
                while (contentNode != null && !(contentNode is Heading && contentNode.level == 2)) {
                    val rendered = renderBlockNode(contentNode)
                    if (rendered.isNotBlank()) {
                        sectionBuilder.append(rendered)
                    }
                    contentNode = contentNode.next
                }
                val value = sectionBuilder.toString().trimEnd()
                if (value.isNotEmpty()) {
                    result[key] = value
                }
                currentNode = contentNode
            } else {
                currentNode = currentNode.next
            }
        }
        return result
    }

    private fun collectHeadingText(heading: Heading): String {
        val builder = StringBuilder()
        var inline: Node? = heading.firstChild
        while (inline != null) {
            builder.append(
                when (inline) {
                    is Text -> inline.literal
                    else -> renderer.render(inline).trim()
                }
            )
            inline = inline.next
        }
        return builder.toString().trim()
    }

    private fun renderBlockNode(node: Node): String {
        return when (node) {
            is Paragraph -> {
                val rendered = renderer.render(node)
                if (rendered.isBlank()) "" else "$rendered\n\n"
            }
            is BulletList, is OrderedList -> {
                val rendered = renderer.render(node)
                if (rendered.isBlank()) "" else "$rendered\n"
            }
            else -> renderer.render(node)
        }
    }

    private fun formatFrontmatterValue(value: Any?): String {
        return when (value) {
            null -> NULL_LITERAL
            is Number, is Boolean -> value.toString()
            else -> value.toString()
        }
    }

    companion object {
        private const val MARKDOWN_TYPE = "Markdown"
        private const val NULL_LITERAL = "null"
    }

    data class ParsedValues(
        val entityId: String,
        val values: MutableMap<AttributeDefId, Any?>
    )
}
