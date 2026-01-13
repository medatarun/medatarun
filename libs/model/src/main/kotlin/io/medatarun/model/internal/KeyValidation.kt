package io.medatarun.model.internal

import io.medatarun.model.domain.KeyEmptyException
import io.medatarun.model.domain.KeyInvalidFormatException
import io.medatarun.model.domain.KeyTooLongException

/**
 * A key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.
 *
 * A valid key must:
 *
 * - be a non-empty string with a bounded length (maximum 128 characters),
 * - contain only printable ASCII characters (code points 0x20 to 0x7E),
 * - not contain any ASCII control characters (0x00–0x1F and 0x7F),
 * - not contain characters with implicit escaping or execution semantics: backslash (\), single quote ('), double quote ("), or backtick (`).
 *
 * No normalization, transformation, or semantic interpretation is applied.
 * The key is compared and stored exactly as provided and is only required to be unique within its enclosing model.
 */
object KeyValidation {
    fun validate(value: String): String {
        if (value.isEmpty()) throw KeyEmptyException()
        if (value.length > MAX_LENGTH) throw KeyTooLongException(MAX_LENGTH)

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

    const val MAX_LENGTH = 128

    const val DESCRIPTION = """
A key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.

A valid key must:

- be a non-empty string with a bounded length (maximum 128 characters),
- contain only printable ASCII characters (code points 0x20 to 0x7E),
- not contain any ASCII control characters (0x00–0x1F and 0x7F),
- not contain characters with implicit escaping or execution semantics: backslash (\), single quote ('), double quote ("), or backtick (`).

No normalization, transformation, or semantic interpretation is applied.
The key is compared and stored exactly as provided.
 """
}