package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.ModelDiffScopeTypeJsonConverter
import io.medatarun.model.domain.diff.ModelDiffScope
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class ModelDiffScopeDescriptor : TypeDescriptor<ModelDiffScope> {
    override val target: KClass<ModelDiffScope> = ModelDiffScope::class
    override val equivMultiplatorm: String = "ModelDiffScope"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: ModelDiffScope): ModelDiffScope {
        return value
    }

    override val description: String = "Defines how model comparison is computed: structural only or full comparison."
    override val jsonConverter: TypeJsonConverter<ModelDiffScope> = ModelDiffScopeTypeJsonConverter()
}
