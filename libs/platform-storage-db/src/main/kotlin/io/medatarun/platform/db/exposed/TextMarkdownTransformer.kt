package io.medatarun.platform.db.exposed

import io.medatarun.type.commons.text.TextMarkdown
import org.jetbrains.exposed.v1.core.ColumnTransformer

class TextMarkdownTransformer : ColumnTransformer<String, TextMarkdown> {
    override fun unwrap(value: TextMarkdown): String {
        return value.name
    }

    override fun wrap(value: String): TextMarkdown {
        return TextMarkdown(value)
    }

}