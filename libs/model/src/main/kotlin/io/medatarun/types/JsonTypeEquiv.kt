package io.medatarun.types

enum class JsonTypeEquiv(val code: String) {
    STRING("string"), BOOLEAN("boolean"), NUMBER("number"), OBJECT("object"), ARRAY("array")
    ;

    companion object {
        private val map = entries.associateBy { it.code }
        fun valueOfCode(code: String): JsonTypeEquiv = map[code] ?: throw ActionParamJsonTypeUnknownException(code)
    }
}