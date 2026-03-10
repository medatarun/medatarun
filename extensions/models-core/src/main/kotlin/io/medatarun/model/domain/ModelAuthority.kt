package io.medatarun.model.domain

import io.medatarun.lang.exceptions.MedatarunException

/**
 * Defines whether a model is a system snapshot or a business reference model.
 */
enum class ModelAuthority(val code: String) {
    /**
     * Model describing a concrete system implementation (often imported).
     */
    SYSTEM("system"),

    /**
     * Model used as a maintained business reference.
     */
    CANONICAL("canonical");

    companion object {
        private val map = entries.associateBy(ModelAuthority::code)

        fun valueOfCodeOptional(code: String): ModelAuthority? {
            return map[code]
        }

        fun valueOfCode(code: String): ModelAuthority {
            return valueOfCodeOptional(code) ?: throw ModelAuthorityIllegalCodeException(code)
        }
    }
}

class ModelAuthorityIllegalCodeException(code: String) :
    MedatarunException("Unknown model authority code: $code")
