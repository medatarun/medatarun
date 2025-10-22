package io.medatarun.data.model

import io.medatarun.model.model.AttributeDefId

interface EntityInitializer {
    fun <T> get(attributeId: AttributeDefId): T
}
