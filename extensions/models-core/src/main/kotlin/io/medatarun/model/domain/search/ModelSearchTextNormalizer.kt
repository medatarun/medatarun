package io.medatarun.model.domain.search

import java.text.Normalizer
import java.util.Locale

/**
 * Search stores one normalized text per indexed object so SQL search uses the same comparison rule everywhere.
 */
fun normalizeModelSearchText(value: String): String {
    val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
    return normalized
        .replace("\\p{M}+".toRegex(), "")
        .lowercase(Locale.ROOT)
        .trim()
}
