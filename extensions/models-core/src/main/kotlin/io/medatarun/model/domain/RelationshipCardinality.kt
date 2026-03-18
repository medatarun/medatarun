package io.medatarun.model.domain

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.type.commons.enums.EnumWithCode

enum class RelationshipCardinality(override val code: String): EnumWithCode {
    ZeroOrOne("zeroOrOne"), Many("many"), One("one"), Unknown("unknown");

    companion object {
        private val map = entries.associateBy(RelationshipCardinality::code)
        fun valueOfCode(code: String) = map[code] ?: throw RelationshipCardinalityIllegalCodeException(code)
    }
}


class RelationshipCardinalityIllegalCodeException(code: String) :
    MedatarunException("Unknown relationship cardinality code: $code")