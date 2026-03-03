package io.medatarun.model.infra.db

enum class SearchItemType(val code: String) {
    MODEL("model"),
    ENTITY("entity"),
    ENTITY_ATTRIBUTE("entity_attribute"),
    RELATIONSHIP("relationship"),
    RELATIONSHIP_ATTRIBUTE("relationship_attribute")
}