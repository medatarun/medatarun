package io.medatarun.model.infra.db.records

import io.medatarun.model.infra.db.ModelStorageDbSearchUnknownItemTypeException
import io.medatarun.type.commons.enums.EnumWithCode

enum class SearchItemType(override val code: String): EnumWithCode {
    MODEL("model"),
    ENTITY("entity"),
    ENTITY_ATTRIBUTE("entity_attribute"),
    RELATIONSHIP("relationship"),
    RELATIONSHIP_ATTRIBUTE("relationship_attribute")

    ;

    companion object {
        val codes = entries.associateBy { it.code }
        fun valueOfCode(code: String): SearchItemType {
            return codes[code] ?: throw ModelStorageDbSearchUnknownItemTypeException(code)
        }
    }
}