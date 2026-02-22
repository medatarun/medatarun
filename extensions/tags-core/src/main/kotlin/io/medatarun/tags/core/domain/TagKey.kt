package io.medatarun.tags.core.domain

import io.medatarun.type.commons.key.Key

@JvmInline
value class TagKey(override val value: String) : Key<TagKey>
