package io.medatarun.auth.domain

@JvmInline
value class Username(val value: String) {
    fun validate():Username {
        if (value.isEmpty()) throw UsernameEmptyException()
        if (value.length < MIN_SIZE) throw UsernameTooShortException(MIN_SIZE)
        if (value.length > MAX_SIZE) throw UsernameTooLongException(MAX_SIZE)
        if (!USERNAME_REGEX.matches(value)) {
            throw UsernameInvalidFormatException()
        }

        return this
    }

    companion object {
        val MIN_SIZE = 3
        val MAX_SIZE = 32
        private val USERNAME_REGEX = Regex(
            "^[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$"
        )
    }
}