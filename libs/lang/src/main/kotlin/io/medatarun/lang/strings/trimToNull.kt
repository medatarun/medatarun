package io.medatarun.lang.strings

fun String?.trimToNull(): String? {
    if (this == null) return null
    val trimmed = trim()
    return if (trimmed.isNullOrEmpty()) null else trimmed
}