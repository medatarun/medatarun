package io.medatarun.data.model

import io.medatarun.model.domain.AttributeDefId

interface EntityInitializer {
    fun <T> get(attributeId: AttributeDefId): T
}
