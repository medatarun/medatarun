package io.medatarun.type.commons.key

interface Key<T : Key<T>> {
    val value: String

    fun asString(): String = value

    @Suppress("UNCHECKED_CAST")
    fun validated(): T {
        KeyValidation.validate(this.value)
        return this as T
    }

    companion object {
        fun <T : Key<T>> fromString(value: String, constructor: (value: String) -> T): T {
            return constructor(value)
        }
    }
}