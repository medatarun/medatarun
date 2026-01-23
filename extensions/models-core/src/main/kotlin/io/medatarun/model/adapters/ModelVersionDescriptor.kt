package io.medatarun.model.adapters

import io.medatarun.model.domain.ModelVersion
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class ModelVersionDescriptor : TypeDescriptor<ModelVersion> {
    override val target: KClass<ModelVersion> = ModelVersion::class
    override val equivMultiplatorm: String = "ModelVersion"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: ModelVersion): ModelVersion {
        return value.validate()
    }

    override val description: String = ModelVersion.Companion.DESCRIPTION
}