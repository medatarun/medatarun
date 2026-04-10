package io.medatarun.model.domain

import io.medatarun.type.commons.enums.EnumWithCode

enum class PBKeyKind(override val code: String) : EnumWithCode {
    PRIMARY("primary"), BUSINESS("business")
}
