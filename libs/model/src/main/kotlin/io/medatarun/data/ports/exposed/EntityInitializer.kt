package io.medatarun.data.ports.exposed

import io.medatarun.model.domain.AttributeDefId

interface EntityInitializer {
    fun <T> get(attributeId: AttributeDefId): T
}
