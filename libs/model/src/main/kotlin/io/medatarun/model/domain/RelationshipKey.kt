package io.medatarun.model.domain

@JvmInline
value class RelationshipKey(val value: String) {
    fun validated(): RelationshipKey {
        return this
    }
}