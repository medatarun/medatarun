package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.ModelVersionTypeJsonConverter
import io.medatarun.model.domain.ModelVersion
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class ModelVersionDescriptor : TypeDescriptor<ModelVersion> {
    override val target: KClass<ModelVersion> = ModelVersion::class
    override val equivMultiplatorm: String = "ModelVersion"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: ModelVersion): ModelVersion {
        return value
    }
    override val jsonConverter: TypeJsonConverter<ModelVersion> = ModelVersionTypeJsonConverter()

    override val description: String = ModelVersion.DESCRIPTION
}
