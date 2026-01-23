import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.RelationshipKey

sealed interface RelationshipRef {

    data class ById(
        val id: RelationshipId
    ) : RelationshipRef

    data class ByKey(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
    ) : RelationshipRef
}