package io.medatarun.model.model

interface LocalizedText {
    val isLocalized: Boolean
    val name: String
    fun get(locale: String): String
    fun all(): Map<String, String>
}


data class LocalizedTextNotLocalized(override val name: String) : LocalizedText {
    override val isLocalized: Boolean = false
    override fun get(locale: String): String = name
    override fun all() = mapOf("default" to name)
}
data class LocalizedTextMap(val values: Map<String, String>) : LocalizedText {
    init {
        if (values.isEmpty()) throw LocalizedTextMapEmptyException()
    }
    override val name: String = values["default"] ?: values["en"] ?: values.entries.first().value
    override fun get(locale: String): String = values[locale] ?: name
    override fun all() = values
    override val isLocalized: Boolean = true
}

typealias LocalizedMarkdown = LocalizedText
typealias LocalizedMarkdownNotLocalized = LocalizedTextNotLocalized
typealias LocalizedMarkdownMap = LocalizedTextMap

class LocalizedTextMapEmptyException: MedatarunException("When creating a LocalizedTextMap you must provide at least one language value or a 'default' key with a value")