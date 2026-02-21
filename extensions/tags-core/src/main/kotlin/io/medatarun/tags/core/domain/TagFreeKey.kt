package io.medatarun.tags.core.domain

import io.medatarun.type.commons.key.Key

@JvmInline
value class TagFreeKey(override val value: String): Key<TagFreeKey>