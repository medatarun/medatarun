package io.medatarun.model.domain

const val DEFAULT_LANG_KEY = "default"
const val DEFAULT_LANG_FALLBACK = "en"

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

data class LocalizedText(override val name: String) : TextBase {
    fun validate(): LocalizedText {
        return this
    }
}

data class LocalizedMarkdown(override val name: String) : TextBase {
    fun validate(): LocalizedMarkdown {
        return this
    }
}


const val TEXT_MARKDOWN_DESCRIPTION = """A rich formatted text."""
const val TEXT_SINGLE_LINE_DESCRIPTION = """A text on a single line, that doesn't exceed 200 characters long."""

