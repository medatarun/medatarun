package io.medatarun.model.domain

import io.medatarun.type.commons.id.Id
import java.util.UUID

@JvmInline
value class EntityPrimaryKeyId(override val value: UUID): Id<EntityPrimaryKeyId> {
    companion object {
        fun generate(): EntityPrimaryKeyId {
            return Id.generate(::EntityPrimaryKeyId)
        }
    }
}
