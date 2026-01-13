package io.medatarun.model.internal

import io.medatarun.model.domain.KeyEmptyException
import io.medatarun.model.domain.KeyInvalidFormatException
import io.medatarun.model.domain.KeyTooLongException

object KeyValidation {
    fun validate(value: String): String {
        if (value.isEmpty()) throw KeyEmptyException()
        if (value.length > 128) throw KeyTooLongException(128)

        value.forEach { ch ->
            val cp = ch.code

            // ASCII imprimable uniquement
            if (cp < 0x20 || cp > 0x7E) {
                throw KeyInvalidFormatException()
            }

            // exclusions ciblées
            when (ch) {
                '\\', '"', '\'', '`' -> throw KeyInvalidFormatException()
            }
        }
        return value
    }
}