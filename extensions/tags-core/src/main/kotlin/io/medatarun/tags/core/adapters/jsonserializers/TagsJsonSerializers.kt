package io.medatarun.tags.core.adapters.jsonserializers

import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.serialization.SerializationUtils.stringSerializer

object TagsJsonSerializers {
    val tagId = stringSerializer("TagId", { TagId.fromString(it) }) { it.value.toString() }
}