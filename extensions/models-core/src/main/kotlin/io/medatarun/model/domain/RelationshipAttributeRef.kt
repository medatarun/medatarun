import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.RelationshipKey

sealed interface RelationshipAttributeRef {

    data class ById(
        val id: AttributeId
    ) : RelationshipAttributeRef

    data class ByKey(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey
    ) : RelationshipAttributeRef
}