package io.medatarun.tags.core.adapters.types

import io.medatarun.tags.core.adapters.json.TagRefJsonConverter
import io.medatarun.tags.core.domain.TagRef
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv

class TagRefTypeDescriptor : TypeDescriptor<TagRef> {
    override val target = TagRef::class
    override val equivMultiplatorm: String = "TagRef"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override val description = """A reference to tag."""
    override val jsonConverter: TypeJsonConverter<TagRef> = TagRefJsonConverter()
    override fun validate(value: TagRef): TagRef {
        return when (value) {
            is TagRef.ById -> value
            is TagRef.ByKey -> {
                when (val scopeRef = value.scopeRef) {
                    is TagScopeRef.Global -> scopeRef
                    is TagScopeRef.Local -> scopeRef
                }
                val localGroupKey = value.groupKey
                if (localGroupKey != null) {
                    localGroupKey.validated()
                }
                value.key.validated()
                value
            }
        }
    }
}
