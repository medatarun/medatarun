package io.medatarun.auth.domain.user

import io.medatarun.auth.domain.UserFullnameEmptyException
import io.medatarun.auth.domain.UserFullnameInvalidFormatException
import io.medatarun.auth.domain.UserFullnameTooLongException

@JvmInline
value class Fullname(val value: String) {
    fun validate(): Fullname {
        val v = value.trim()
        if (v.isEmpty()) throw UserFullnameEmptyException()
        if (v.length > 200) throw UserFullnameTooLongException()

        v.codePoints().forEach { cp ->
            if (!isAllowed(cp)) throw UserFullnameInvalidFormatException()
        }
        return this
    }
    private fun isAllowed(cp: Int): Boolean {
        // séparateurs usuels
        if (cp == 0x20) return true // espace ASCII
        if (cp == '-'.code || cp == '\''.code || cp == '.'.code || cp == ','.code) return true

        // lettres + diacritiques (accents combinants)
        val t = Character.getType(cp)
        if (t == Character.UPPERCASE_LETTER.toInt() ||
            t == Character.LOWERCASE_LETTER.toInt() ||
            t == Character.TITLECASE_LETTER.toInt() ||
            t == Character.MODIFIER_LETTER.toInt() ||
            t == Character.OTHER_LETTER.toInt() ||
            t == Character.NON_SPACING_MARK.toInt() ||
            t == Character.COMBINING_SPACING_MARK.toInt() ||
            t == Character.ENCLOSING_MARK.toInt()
        ) return true

        return false
    }
}