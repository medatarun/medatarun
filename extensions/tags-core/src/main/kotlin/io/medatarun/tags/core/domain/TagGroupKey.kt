package io.medatarun.tags.core.domain

import io.medatarun.type.commons.key.Key
import io.medatarun.type.commons.key.KeyStrictValidation

@JvmInline
value class TagGroupKey(override val value: String) : Key<TagGroupKey> {
    override fun validated(): TagGroupKey {
        KeyStrictValidation.validate(value)
        return this
    }
}
