package io.medatarun.model.domain

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.type.commons.enums.EnumWithCode

enum class ModelSnapshotKind(override val code: String) : EnumWithCode {
    CURRENT_HEAD("CURRENT_HEAD"),
    VERSION_SNAPSHOT("VERSION_SNAPSHOT");

    companion object {
        private val map = entries.associateBy(ModelSnapshotKind::code)

        fun valueOfCodeOptional(code: String): ModelSnapshotKind? {
            return map[code]
        }

        fun valueOfCode(code: String): ModelSnapshotKind {
            return valueOfCodeOptional(code) ?: throw ModelSnapshotKindIllegalCodeException(code)
        }
    }
}

class ModelSnapshotKindIllegalCodeException(code: String) :
    MedatarunException("Unknown model snapshot kind code: $code")
