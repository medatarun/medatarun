package io.medatarun.model.domain

/**
 * Base type for all localizable texts.
 * Wrapper around some texts.
 */
sealed interface TextBase {

    /**
     * Returns a default text, when we don't need localization
     */
    val name: String

}

data class TextSingleLine(override val name: String) : TextBase {
    fun validate(): TextSingleLine {
        return this
    }
}

data class TextMarkdown(override val name: String) : TextBase {
    fun validate(): TextMarkdown {
        return this
    }
}


const val TEXT_MARKDOWN_DESCRIPTION = """A rich formatted text."""
const val TEXT_SINGLE_LINE_DESCRIPTION = """A text on a single line, that doesn't exceed 200 characters long."""

