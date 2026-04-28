package io.medatarun.ext.modeljson.internal.serializers

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText

data class LocalizedTextMultiLangCompat(val value : String ) {
    fun toLocalizedText() = LocalizedText(value)
    fun toLocalizedMarkdown() = LocalizedMarkdown(value)
    companion object {
        fun of(other: LocalizedText) = LocalizedTextMultiLangCompat(other.name)
        fun ofOptional(other: LocalizedText?) = other?.name?.let { LocalizedTextMultiLangCompat(it) }
        fun of(other: LocalizedMarkdown) = LocalizedTextMultiLangCompat(other.name)
        fun ofOptional(other: LocalizedMarkdown?) = other?.name?.let { LocalizedTextMultiLangCompat(it) }
    }
}