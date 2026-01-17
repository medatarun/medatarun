package io.medatarun.model.domain

import io.medatarun.lang.exceptions.MedatarunException

enum class RelationshipCardinality(val code: String) {
    ZeroOrOne("zeroOrOne"), Many("many"), One("one"), Unknown("unknown");
    companion object {
        private val map = entries.associateBy(RelationshipCardinality::code)
        fun valueOfCode(code: String) = map[code] ?: throw RelationshipCardinalityIllegalCodeException(code)
    }
}


class RelationshipCardinalityIllegalCodeException(code: String): MedatarunException("Unknown relationship cardinality code: $code")