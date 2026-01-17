package io.medatarun.model.domain

import java.util.*

const val DEFAULT_LANG_KEY = "default"
const val DEFAULT_LANG_FALLBACK = "en"

/**
 * Base type for all localizable texts.
 * Wrapper around some texts.
 */
sealed interface LocalizedText {
    /**
     * indicates that this text has localized variants
     */
    val isLocalized: Boolean

    /**
     * Returns a default text, when we don't need localization
     */
    val name: String

    /**
     * Returns text with the given language or the default text or the default fallback language
     */
    fun get(locale: String): String

    /**
     * Returns a map with all translations
     */
    fun all(): Map<String, String>

    fun get(locale: Locale): String {
        return get(locale.language)
    }

}

/**
 * Specialized LocalizedText that doesn't contain localization, just a default text
 */
data class LocalizedTextNotLocalized(override val name: String) : LocalizedText {
    override val isLocalized: Boolean = false
    override fun get(locale: String): String = name
    override fun all() = mapOf(DEFAULT_LANG_KEY to name)
}

/**
 * Specialized LocalizedText that contains translations.
 */
data class LocalizedTextMap(val values: Map<String, String>) : LocalizedText {
    init {
        if (values.isEmpty()) throw LocalizedTextMapEmptyException()
    }

    override val name: String =
        values[DEFAULT_LANG_KEY] ?: values[DEFAULT_LANG_FALLBACK] ?: values.entries.first().value

    override fun get(locale: String): String = values[locale] ?: name
    override fun all() = values
    override val isLocalized: Boolean = true
}

typealias LocalizedMarkdown = LocalizedText
typealias LocalizedMarkdownNotLocalized = LocalizedTextNotLocalized
typealias LocalizedMarkdownMap = LocalizedTextMap
