package io.medatarun.tags.core.domain

import io.medatarun.type.commons.key.Key
import io.medatarun.type.commons.key.KeyStrictValidation

@JvmInline
value class TagKey(override val value: String) : Key<TagKey> {
    override fun validated(): TagKey {
        KeyStrictValidation.validate(value)
        return this
    }
}
