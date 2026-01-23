import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.ModelKey

sealed interface EntityAttributeRef {

    data class ById(
        val id: AttributeId
    ) : EntityAttributeRef

    data class ByKey(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey
    ) : EntityAttributeRef
}