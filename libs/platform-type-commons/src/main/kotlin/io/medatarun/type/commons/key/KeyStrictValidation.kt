package io.medatarun.type.commons.key

/**
 * Strict key validation for user-facing keys such as tags.
 *
 * Allowed characters are intentionally limited to letters, digits, '_' and '-'.
 * Case is preserved.
 */
object KeyStrictValidation {
    const val MAX_LENGTH = 128

    fun validate(value: String): String {
        if (value.isEmpty()) throw KeyStrictEmptyException()
        if (value.length > MAX_LENGTH) throw KeyStrictTooLongException(MAX_LENGTH)

        value.forEach { ch ->
            if (!isAllowed(ch)) {
                throw KeyStrictInvalidFormatException()
            }
        }
        return value
    }

    private fun isAllowed(ch: Char): Boolean {
        return ch in 'a'..'z' ||
            ch in 'A'..'Z' ||
            ch in '0'..'9' ||
            ch == '_' ||
            ch == '-'
    }
}
