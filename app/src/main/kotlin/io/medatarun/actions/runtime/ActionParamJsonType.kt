package io.medatarun.actions.runtime

enum class ActionParamJsonType(val code: String) {
    STRING("string"), BOOLEAN("boolean"), NUMBER("number"), OBJECT("object"), ARRAY("array")
    ;

    companion object {
        private val map = entries.associateBy { it.code }
        fun valueOf(code: String): ActionParamJsonType = map[code] ?: throw ActionParamJsonTypeUnknownException(code)
    }
}