package io.medatarun.type.commons.text

data class TextMarkdown(override val name: String) : TextBase {
    fun validate(): TextMarkdown {
        return this
    }
}

const val TEXT_MARKDOWN_DESCRIPTION = """A rich formatted text."""