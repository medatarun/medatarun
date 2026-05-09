package io.medatarun.type.commons.text

data class TextSingleLine(override val name: String) : TextBase {
    fun validate(): TextSingleLine {
        return this
    }
}

const val TEXT_SINGLE_LINE_DESCRIPTION = """A text on a single line, that doesn't exceed 200 characters long."""