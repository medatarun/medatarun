package io.medatarun.data.ports.exposed

import io.medatarun.model.domain.AttributeKey

interface EntityInitializer {
    fun <T> get(attributeId: AttributeKey): T
}
