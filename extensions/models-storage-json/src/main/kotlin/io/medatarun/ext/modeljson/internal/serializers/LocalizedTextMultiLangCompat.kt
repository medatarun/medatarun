package io.medatarun.ext.modeljson.internal.serializers

import io.medatarun.model.domain.TextMarkdown
import io.medatarun.model.domain.TextSingleLine

data class LocalizedTextMultiLangCompat(val value : String ) {
    fun toTextSingleLine() = TextSingleLine(value)
    fun toTextMarkdown() = TextMarkdown(value)
    companion object {
        fun of(other: TextSingleLine) = LocalizedTextMultiLangCompat(other.name)
        fun ofOptional(other: TextSingleLine?) = other?.name?.let { LocalizedTextMultiLangCompat(it) }
        fun of(other: TextMarkdown) = LocalizedTextMultiLangCompat(other.name)
        fun ofOptional(other: TextMarkdown?) = other?.name?.let { LocalizedTextMultiLangCompat(it) }
    }
}