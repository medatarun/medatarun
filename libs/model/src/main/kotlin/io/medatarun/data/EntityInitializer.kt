package io.medatarun.data

import io.medatarun.model.model.ModelAttributeId

interface EntityInitializer {
    fun <T> get(attributeId: ModelAttributeId): T
}
