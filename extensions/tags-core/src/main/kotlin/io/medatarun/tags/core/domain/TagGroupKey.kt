package io.medatarun.tags.core.domain

import io.medatarun.type.commons.key.Key

@JvmInline
value class TagGroupKey(override val value: String) : Key<TagGroupKey>
