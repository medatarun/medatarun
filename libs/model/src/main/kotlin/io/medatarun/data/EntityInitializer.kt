package io.medatarun.data

import io.medatarun.model.model.AttributeDefId

interface EntityInitializer {
    fun <T> get(attributeId: AttributeDefId): T
}
