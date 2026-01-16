package io.medatarun.types

enum class TypeJsonEquiv(val code: String) {
    STRING("string"), BOOLEAN("boolean"), NUMBER("number"), OBJECT("object"), ARRAY("array")
    ;

    companion object {
        private val map = entries.associateBy { it.code }
        fun valueOfCode(code: String): TypeJsonEquiv = map[code] ?: throw TypeJsonEquivUnknownException(code)
    }
}