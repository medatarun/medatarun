package io.medatarun.tags.core.adapters

import io.medatarun.tags.core.adapters.json.TagFreeRefJsonConverter
import io.medatarun.tags.core.domain.TagFreeKey
import io.medatarun.tags.core.domain.TagFreeRef
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class TagFreeRefTypeDescriptor : TypeDescriptor<TagFreeRef> {
    override val target = TagFreeRef::class
    override val equivMultiplatorm: String = "TagFreeRef"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override val description = """A reference to free tag."""
    override val jsonConverter: TypeJsonConverter<TagFreeRef> = TagFreeRefJsonConverter()
    override fun validate(value: TagFreeRef): TagFreeRef {
        return when (value) {
            is TagFreeRef.ById -> value
            is TagFreeRef.ByKey -> {
                value.key.validated()
                value
            }
        }
    }
}