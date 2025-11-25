package io.medatarun.lang

fun String?.trimToNull(): String? {
    if (this == null) return null
    val trimmed = trim()
    return if (trimmed.isNullOrEmpty()) null else trimmed
}